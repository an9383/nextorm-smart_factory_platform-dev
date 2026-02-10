package com.nextorm.collector.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HttpEcopeaceCollector implements Collector {
	private final ObjectMapper mapper;
	private final DataCollectPlan config;
	private final Sender sender;

	private final List<String> collectParameterNames;
	private final HttpClient httpClient;
	private final HttpRequest httpRequest;
	private String previousDateTime = "";

	public HttpEcopeaceCollector(
		DataCollectPlan config,
		ObjectMapper objectMapper,
		Sender sender
	) {
		this.mapper = objectMapper;
		this.config = config;
		this.sender = sender;

		String apiUrl = config.getCollectorArguments()
							  .get("apiUrl")
							  .toString();

		this.httpClient = HttpClient.newHttpClient();
		this.httpRequest = HttpRequest.newBuilder()
									  .uri(URI.create(apiUrl))
									  .build();

		this.collectParameterNames = config.getCollectParameters()
										   .stream()
										   .map(DataCollectPlan.Parameter::getName)
										   .toList();
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

			try {
				Thread.sleep(config.getDataInterval() * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread()
					  .interrupt();
				// throw new RuntimeException(e);
			}

		}
	}

	private Map<String, Object> getData() {
		Map<String, Object> data = new HashMap<>();

		try {
			HttpResponse<String> response = requestEcopeaceApi();
			String result = response.body();

			log.info("response body: {}", result);

			Map<String, Object> datas = jsonStringToObj(result, Map.class);
			if (Objects.isNull(datas)) {
				return Map.of();
			}

			addGeoData(data, datas);

			for (String name : collectParameterNames) {
				Object value = datas.get(name);
				data.put(name, value);
			}

			return data;
		} catch (UnresolvedAddressException e) {
			log.error("getData Error, TOOL ID: {}, DCP ID: {}", config.getToolId(), config.getDcpId(), e);
			throw new RuntimeException(e);
		}
	}

	private HttpResponse<String> requestEcopeaceApi() {
		int maxRetryCount = 5;
		int retryCount = 0;
		long retryInterval = 3000L;
		try {
			HttpResponse<String> response = null;
			while (response == null) {
				try {
					response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
					break;
				} catch (ConnectException e) {
					log.warn("ConnectException 발생. 재시도합니다. 현재: {}, {}", retryCount, e.getMessage());
					if (retryCount >= maxRetryCount) {
						log.error("ConnectException 발생. 재시도 횟수 초과. 재시도를 종료합니다. url: {}", httpRequest.uri());
						throw e;
					}
					retryCount++;
					Thread.sleep(retryInterval);
				}
			}
			return response;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/*
		Geo 타입일때 위도, 경도 데이터 추가
	 */
	private void addGeoData(
		Map<String, Object> data,
		Map<String, Object> datas
	) {
		if (!Boolean.TRUE.equals(config.getIsGeoDataType())) {
			return;
		}
		String latitudeParameterName = config.getLatitudeParameterName();
		String longitudeParameterName = config.getLongitudeParameterName();

		if (latitudeParameterName != null) {
			Double latitudeValue = (Double)datas.get(latitudeParameterName);
			if (latitudeValue == null) {
				throw new IllegalArgumentException(String.format("Dcp Id: %s에 %s 위도값이 존재하지 않습니다.",
					this.config.getDcpId(),
					latitudeParameterName));
			}
			data.put(latitudeParameterName, latitudeValue);
		}

		if (longitudeParameterName != null) {
			Double longitudeValue = (Double)datas.get(longitudeParameterName);
			if (longitudeValue == null) {
				throw new IllegalArgumentException(String.format("Dcp Id: %s에 %s 경도값이 존재하지 않습니다.",
					this.config.getDcpId(),
					longitudeParameterName));
			}
			data.put(longitudeParameterName, longitudeValue);
		}
	}

	public <T> T jsonStringToObj(
		String jsonString,
		Class<T> objType
	) {
		if (jsonString == null) {
			return null;
		}
		try {
			String compareData = compareFirstElementWithPrevious(jsonString);
			if (Objects.isNull(compareData)) {
				return null;
			}
			return mapper.readValue(compareData, objType);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String compareFirstElementWithPrevious(String jsonString) {
		JSONArray jsonArray = new JSONArray(jsonString);
		JSONObject latestJSONObject = null;
		String lastestTimestamp = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String timestamp = jsonObject.getString("timestamp");
			if (lastestTimestamp == null || timestamp.compareTo(lastestTimestamp) > 0) {
				lastestTimestamp = timestamp;
				latestJSONObject = jsonObject;
			}
		}
		if (!previousDateTime.isEmpty() && latestJSONObject.get("timestamp")
														   .equals(previousDateTime)) {
			return null;
		}
		previousDateTime = String.valueOf(latestJSONObject.get("timestamp"));

		return latestJSONObject.toString();
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();
		if (Boolean.TRUE.equals(config.getIsGeoDataType())) {
			collectParameterSize = collectParameterSize + 2;    // Geo Type 일때 파라미터 size 2 추가
		}
		if (paramsValueMap.size() != collectParameterSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
				collectParameterSize,
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
