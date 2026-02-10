package com.nextorm.collector.collector;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RestApiCollector implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;

	private final HttpClient httpClient;
	private final HttpRequest httpRequest;

	private final Map<String, JsonPath> collectParameterCompiledJsonPaths;

	public RestApiCollector(
		DataCollectPlan config,
		Sender sender
	) {
		this.config = config;
		this.sender = sender;

		String url = config.getCollectorArguments()
						   .get("url")
						   .toString();

		String httpMethod = config.getCollectorArguments()
								  .get("method")
								  .toString()
								  .toUpperCase();

		String encodedUrl = encodeUrlExceptBase(url);

		this.httpClient = HttpClient.newBuilder()
									.connectTimeout(Duration.ofSeconds(10))
									.build();

		this.httpRequest = HttpRequest.newBuilder()
									  .uri(URI.create(encodedUrl))
									  .method(httpMethod, HttpRequest.BodyPublishers.noBody())
									  .build();

		this.collectParameterCompiledJsonPaths = new HashMap<>();
		config.getCollectParameters()    // JSONPath 컴파일
			  .forEach(parameter -> {
				  String jsonPath = parameter.getExtraData()
											 .get("jsonPath")
											 .toString();
				  this.collectParameterCompiledJsonPaths.put(parameter.getName(), JsonPath.compile(jsonPath));
			  });
	}

	@Override
	public DataCollectPlan getConfig() {
		return this.config;
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

	public Map<String, Object> getData() {
		HttpResponse<String> response = null;
		try {
			response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			log.error("HTTP 요청 중 오류 발생: URL = {}", httpRequest.uri(), e);
			return Map.of();
		}

		if (response.statusCode() != HttpStatus.OK.value()) {
			log.error("HTTP 요청 실패: 상태 코드 = {}, URL = {}", response.statusCode(), httpRequest.uri());
			return Map.of();
		}

		String responseBody = response.body();
		ReadContext context = JsonPath.parse(responseBody);

		// 응답 데이터를 Map으로 변환하여 반환
		Map<String, Object> resultMap = new HashMap<>();
		collectParameterCompiledJsonPaths.forEach((parameterName, jsonPath) -> {
			Object parseValue = parseValue(context, jsonPath);
			resultMap.put(parameterName, parseValue);
		});
		return resultMap;
	}

	/**
	 * JSONPath를 사용하여 JSON 데이터를 읽고, JsonPath에 해당하는 값이 없는 경우, null을 반환합니다.
	 *
	 * @param context  JSONPath를 사용할 ReadContext
	 * @param jsonPath 읽을 JSONPath
	 * @return JSONPath에 해당하는 값, 오류 발생 시 null
	 */
	private Object parseValue(
		ReadContext context,
		JsonPath jsonPath
	) {
		try {
			return context.read(jsonPath);
		} catch (PathNotFoundException e) {
			log.error("Error reading JSON path {}: {}", jsonPath, e.getMessage());
			return null;
		}
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int paramSize = config.getCollectParameters()
							  .size();

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

	/**
	 * 주어진 URL을 파싱하여 baseUrl(스키마와 호스트 부분)을 제외한 나머지 경로와 쿼리 파라미터를 인코딩합니다.
	 *
	 * @param url 전체 URL 문자열
	 * @return baseUrl과 인코딩된 경로 및 쿼리 파라미터를 결합한 URL
	 */
	private String encodeUrlExceptBase(String url) {
		try {
			// URL을 수동으로 파싱하여 baseUrl, path, query를 분리
			int pathStartIndex = url.indexOf("/", url.indexOf("//") + 2);
			String baseUrl = pathStartIndex != -1
							 ? url.substring(0, pathStartIndex)
							 : url;
			String pathAndQuery = pathStartIndex != -1
								  ? url.substring(pathStartIndex)
								  : "";

			// path와 query를 분리
			String path = pathAndQuery.contains("?")
						  ? pathAndQuery.substring(0, pathAndQuery.indexOf("?"))
						  : pathAndQuery;

			String query = pathAndQuery.contains("?")
						   ? pathAndQuery.substring(pathAndQuery.indexOf("?") + 1)
						   : "";

			// 경로와 쿼리 파라미터 인코딩
			String encodedPath = encodePath(path);
			String encodedQuery = encodeQuery(query);

			// 인코딩된 URL 반환
			return baseUrl + encodedPath + (encodedQuery.isEmpty()
											? ""
											: "?" + encodedQuery);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid URL: " + url, e);
		}
	}

	private String encodePath(String path) {
		return URLEncoder.encode(path, StandardCharsets.UTF_8)
						 .replaceAll("%2F", "/")
						 .replaceAll("\\+", "%20")
						 .replaceAll("\\%21", "!")
						 .replaceAll("\\%27", "'")
						 .replaceAll("\\%28", "(")
						 .replaceAll("\\%29", ")")
						 .replaceAll("\\%7E", "~");
	}

	private String encodeQuery(String query) {
		return query.isEmpty()
			   ? ""
			   : URLEncoder.encode(query, StandardCharsets.UTF_8)
						   .replaceAll("%3D", "=")
						   .replaceAll("%26", "&")
						   .replaceAll("\\+", "%20")
						   .replaceAll("\\%21", "!")
						   .replaceAll("\\%27", "'")
						   .replaceAll("\\%28", "(")
						   .replaceAll("\\%29", ")")
						   .replaceAll("\\%7E", "~");
	}
}