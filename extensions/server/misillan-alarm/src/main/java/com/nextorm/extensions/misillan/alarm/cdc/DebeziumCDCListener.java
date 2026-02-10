package com.nextorm.extensions.misillan.alarm.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConditionalOnProperty(value = "cdc.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Component
@Slf4j
public class DebeziumCDCListener {
	@Getter
	@RequiredArgsConstructor
	enum Operation {
		CREATE("c"), UPDATE("u"), DELETE("d"), READ("r"); // snapshot read

		private final String code;

		private static final Map<String, Operation> BY_CODE = Arrays.stream(values())
																	.collect(Collectors.toMap(Operation::getCode,
																		Function.identity()));

		// valueOf 대신 사용할 정적 팩토리 메서드
		public static Operation fromStruct(Struct struct) {
			String code = struct.getString("op");
			if (code == null) {
				throw new IllegalArgumentException("Struct 'op' field is null");
			}

			Operation op = BY_CODE.get(code);
			if (op == null) {
				throw new IllegalArgumentException("Unknown code: " + code);
			}
			return op;
		}
	}

	private final Configuration debeziumConfig;
	private final ObjectMapper objectMapper;
	private final AlarmDetectService alarmDetectService;

	private final Executor executor = Executors.newSingleThreadExecutor();
	private DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

	@PostConstruct
	public void startDebeziumEngine() {
		this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
											.using(debeziumConfig.asProperties())
											.notifying(this::handleChangeEvent)
											.build();

		executor.execute(debeziumEngine);
		log.info("Debezium engine started successfully");
	}

	@PreDestroy
	public void stopDebeziumEngine() {
		try {
			if (debeziumEngine != null) {
				debeziumEngine.close();
			}
		} catch (IOException e) {
			log.error("Failed to stop Debezium engine", e);
		}
	}

	private void handleChangeEvent(RecordChangeEvent<SourceRecord> event) {
		SourceRecord sourceRecord = event.record();
		String topic = sourceRecord.topic();

		Struct struct = (Struct)sourceRecord.value();
		if (struct == null) {
			log.debug("Skipping event with null value from topic: {}", topic);
			return;
		}

		// 오퍼레이션 필드 (CRUD) 존재여부 확인, 없으면 스키마 변경이나 DDL 이벤트로 간주하고 무시
		if (!existOperationField(struct)) {
			log.debug("Skipping event without 'op' field from topic: {} (likely schema/DDL event)", topic);
			return;
		}

		Operation operation = Operation.fromStruct(struct);
		String tableName = extractTableName(topic);

		try {
			switch (operation) {
				case CREATE:
					if (tableName.equals("parameter_data")) {
						ParameterData parameterData = parseAfterStructToParameterData(struct.getStruct("after"));
						alarmDetectService.processAlarmDetection(parameterData);
					}
					break;
				case READ, UPDATE, DELETE:
					log.debug("Skipping unsupported operation: {} for table: {}", operation, tableName);
					break;
				default:
					log.warn("Unknown operation: {}", operation);
			}
		} catch (Exception e) {
			log.error("Error processing change event for table: {}", tableName, e);
		}
	}

	private ParameterData parseAfterStructToParameterData(Struct struct) {
		return objectMapper.convertValue(extractData(struct), ParameterData.class);
	}

	private boolean existOperationField(Struct struct) {
		if (struct == null) {
			return false;
		}
		Field opField = struct.schema()
							  .field("op");
		return opField != null;
	}

	private String extractTableName(String topic) {
		String[] parts = topic.split("\\.");
		return parts[parts.length - 1];
	}

	private Map<String, Object> extractData(Struct struct) {
		Map<String, Object> data = new HashMap<>();

		for (Field field : struct.schema()
								 .fields()) {
			Object value = struct.get(field);
			data.put(field.name(), value);
		}
		return data;
	}
}
