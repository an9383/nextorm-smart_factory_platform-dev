package com.nextorm.collector.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpEmulatorCollector implements Collector {
	private final ObjectMapper objectMapper;
	private final DataCollectPlan config;
	private final Sender sender;

	private final HttpClient httpClient;
	private final HttpRequest httpRequest;

	private final List<String> collectParameters;

	public HttpEmulatorCollector(
		DataCollectPlan config,
		ObjectMapper objectMapper,
		Sender sender
	) {
		this.objectMapper = objectMapper;
		this.config = config;
		this.sender = sender;

		String apiUrl = config.getCollectorArguments()
							  .get("apiUrl")
							  .toString();

		this.httpClient = HttpClient.newHttpClient();
		this.httpRequest = HttpRequest.newBuilder()
									  .uri(URI.create(apiUrl))
									  .build();

		this.collectParameters = config.getCollectParameters()
									   .stream()
									   .map(DataCollectPlan.Parameter::getName)
									   .toList();
	}

	@Override
	public void run() {
		while (true) {
			SendMessage collect = collect();
			sender.send(config.getTopic(), collect);
			try {
				Thread.sleep(config.getDataInterval() * 1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public SendMessage collect() {
		try {
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return convert(response.body());
		} catch (IOException | InterruptedException e) {
			log.error("getData Error, TOOL ID: {}, DCP ID: {}", config.getToolId(), config.getDcpId(), e);
			throw new RuntimeException(e);
		}
	}

	private SendMessage convert(String collectMessage) {
		Map<String, Object> parameterValueMap = new HashMap<>();

		Map<String, Object> collectData = toMap(collectMessage);

		addGeoData(parameterValueMap, collectData);

		for (String name : collectParameters) {
			Object value = collectData.get(name);
			parameterValueMap.put(name, value);
		}
		return paramValuesToMessage(parameterValueMap);
	}

	/*
		Geo 타입일때 위도, 경도 데이터 추가
	 */
	private void addGeoData(
		Map<String, Object> parameterValueMap,
		Map<String, Object> collectData
	) {
		if (!Boolean.TRUE.equals(config.getIsGeoDataType())) {
			return;
		}

		String latitudeParameterName = config.getLatitudeParameterName();
		String longitudeParameterName = config.getLongitudeParameterName();

		if (latitudeParameterName != null) {
			Double latitudeValue = (Double)collectData.get(config.getLatitudeParameterName());
			if (latitudeValue == null) {
				throw new IllegalArgumentException(String.format("Dcp Id: %s에 %s 위도값이 존재하지 않습니다.",
					this.config.getDcpId(),
					latitudeParameterName));
			}
			parameterValueMap.put(latitudeParameterName, latitudeValue);
		}
		if (longitudeParameterName != null) {
			Double longitudeValue = (Double)collectData.get(config.getLongitudeParameterName());
			if (longitudeValue == null) {
				throw new IllegalArgumentException(String.format("Dcp Id: %s에 %s 경도값이 존재하지 않습니다.",
					this.config.getDcpId(),
					longitudeParameterName));
			}
			parameterValueMap.put(longitudeParameterName, longitudeValue);
		}

	}

	private Map<String, Object> toMap(String jsonString) {
		if (jsonString == null) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(jsonString, Map.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int paramSize = collectParameters.size();
		if (Boolean.TRUE.equals(config.getIsGeoDataType())) {
			paramSize = paramSize + 2;    // Geo Type 일때 파라미터 size 2 추가
		}
		if (paramsValueMap.size() != paramSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(paramSize,
				paramsValueMap.size());
			throw new IllegalArgumentException(message);
		}

		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}
}
