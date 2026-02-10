package com.nextorm.collector.collector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RoboshotMonitorCollector implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;
	private final ObjectMapper objectMapper;

	private final List<DataCollectPlan.Parameter> collectParameters;

	//서버 IP
	private final String ip;

	// Spring Boot Port
	private final String port;
	//화낙 ROBOSHOT 기계 ID
	private final int machineId;
	// 데이터 받아오는 exe파일 경로
	private final String exePath;

	// 이전 Shot 수 값
	private Double previousShotCount = null;

	public RoboshotMonitorCollector(
		DataCollectPlan config,
		Sender sender,
		ObjectMapper objectMapper
	) {
		this.config = config;
		this.sender = sender;
		this.objectMapper = objectMapper;

		ip = config.getCollectorArguments()
				   .get("ip")
				   .toString();
		port = config.getCollectorArguments()
					 .get("port")
					 .toString();
		machineId = Integer.parseInt(config.getCollectorArguments()
										   .get("machineId")
										   .toString());
		exePath = config.getCollectorArguments()
						.get("exePath")
						.toString();

		collectParameters = config.getCollectParameters();
	}

	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {

			try {
				Map<String, Object> data = getData();
				if (!data.isEmpty()) {
					SendMessage sendMessage = paramValuesToMessage(data);
					sender.send(config.getTopic(), sendMessage);
				}
			} catch (RuntimeException e) {
				log.error("컬렉터 종료 가능 에러가 발생. DCP_ID: {}", config.getDcpId(), e);
			}

			sleep(config.getDataInterval());
		}
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();

		if (paramsValueMap.size() != collectParameterSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
				collectParameterSize,
				paramsValueMap.size());
			throw new IllegalArgumentException(message);
		}

		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	private void sleep(int intervalSeconds) {
		try {
			Thread.sleep(intervalSeconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread()
				  .interrupt();
		}
	}

	private Map<String, Object> getData() {
		Map<String, Object> data = new HashMap<>();
		String apiUrl = String.format("http://%s:%s/api/monitor/data", ip, port);
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setDoOutput(true);

			try (var outputStream = connection.getOutputStream()) {
				outputStream.write(objectMapper.writeValueAsBytes(createRequestBody()));
				outputStream.flush();
			}

			int responseCode = connection.getResponseCode();
			if (responseCode >= 400) {
				log.error("HTTP 요청 실패: {} - {}", responseCode, connection.getResponseMessage());
				try (InputStream errorStream = connection.getErrorStream()) {
					if (errorStream != null) {
						String errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
						log.error("서버 응답 오류 메시지: {}", errorMessage);
					}
				}
			} else {
				try (InputStream inputStream = connection.getInputStream()) {
					List<String> response = objectMapper.readValue(inputStream, new TypeReference<>() {
					});
					for (DataCollectPlan.Parameter detail : collectParameters) {
						MappingData mappingData = objectMapper.convertValue(detail.getExtraData(), MappingData.class);
						String parameter = detail.getName();

						String rawValue = response.get(mappingData.dataIndex() - 1);
						Double parameterData = (rawValue != null && !rawValue.trim()
																			 .isEmpty())
											   ? Double.valueOf(rawValue.trim())
											   : 0.0;

						// 이전 SHOT수와 같지않을때만 데이터를 전송
						if ("SHOT수".equals(parameter)) {
							if (previousShotCount != null && previousShotCount.equals(parameterData)) {
								return new HashMap<>();
							}
							previousShotCount = parameterData;
						}

						data.put(parameter, parameterData);
					}
				}
			}
			connection.disconnect();
			return data;
		} catch (Exception e) {
			log.error("데이터 읽기 중 예외 발생", e);
			return new HashMap<>();
		}
	}

	private Map<String, Object> createRequestBody() {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("machineId", machineId);
		requestBody.put("exePath", exePath);
		return requestBody;
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}

	record MappingData(int dataIndex) {
	}

}
