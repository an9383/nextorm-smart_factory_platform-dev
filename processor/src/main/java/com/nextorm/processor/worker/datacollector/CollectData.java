package com.nextorm.processor.worker.datacollector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectData {
	private static final String DCP_ID_KEY = "dcpId";
	private static final String TRACE_AT_KEY = "dateTime";
	private static final String VALUES_KEY = "values";

	private Long dcpId;
	private LocalDateTime traceAt;
	private Map<String, Object> values;

	public static List<CollectData> listOfConsumerRecords(ConsumerRecords<String, Map<String, Object>> records) {
		return StreamSupport.stream(records.spliterator(), false)
							.map(consumerRecord -> of(consumerRecord.value()))
							.toList();
	}

	private static CollectData of(Map<String, Object> recordValue) {
		return new CollectData(parseDcpId(recordValue), parseTraceAt(recordValue), parseValues(recordValue));
	}

	private static Map<String, Object> parseValues(Map<String, Object> collectData) {
		return (Map<String, Object>)collectData.get(VALUES_KEY);
	}

	private static long parseDcpId(Map<String, Object> collectData) {
		return Long.parseLong(collectData.get(DCP_ID_KEY)
										 .toString());
	}

	private static LocalDateTime parseTraceAt(Map<String, Object> collectData) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)collectData.get(TRACE_AT_KEY)),
			ZoneId.systemDefault());
	}

	public Object getValueByParameterName(String parameterName) {
		return values.get(parameterName);
	}
}
