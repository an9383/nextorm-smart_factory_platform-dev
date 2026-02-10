package com.nextorm.collector.configprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.collector.CollectorType;
import com.nextorm.collector.properties.PortalProperties;
import com.nextorm.common.define.collector.CollectorArgument;
import com.nextorm.common.define.collector.CollectorTypeDefineRequestDto;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HttpConfigProvider implements ConfigProvider {
	private static final String COLLECTOR_TYPE_DEFINITION_API_PATH = "/api/config/collector/type-definition";

	private final ObjectMapper objectMapper;
	private final ConfigProviderArguments applicationConfig;
	private final PortalProperties portalProperties;

	private List<DataCollectPlan> config;

	public HttpConfigProvider(
		ObjectMapper objectMapper,
		ConfigProviderArguments applicationConfig,
		PortalProperties portalProperties
	) {
		this.objectMapper = objectMapper;
		this.applicationConfig = applicationConfig;
		this.portalProperties = portalProperties;

		dispatchCollectorKind();
		requestConfig(applicationConfig.getConfigServerAddress(), applicationConfig.getConfigName());
	}

	private void dispatchCollectorKind() {
		CollectorTypeDefineRequestDto requestBody = createRequestBody();
		log.info("Collector Type Definition: {}", requestBody);

		String apiUrl = applicationConfig.getConfigServerAddress() + COLLECTOR_TYPE_DEFINITION_API_PATH;
		HttpRequest request = HttpRequest.newBuilder()
										 .uri(URI.create(apiUrl))
										 .header("Authorization",
											 String.format("Bearer %s", portalProperties.getApiToken()))
										 .POST(HttpRequest.BodyPublishers.ofString(toJsonString(requestBody)))
										 .header("Content-Type", "application/json;utf-8")
										 .build();
		HttpClient client = HttpClient.newHttpClient();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("statusCode: {}", response.statusCode());

			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to dispatch collector kind. httpStatus=" + response.statusCode() + ", body=" + response.body());
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private CollectorTypeDefineRequestDto createRequestBody() {
		CollectorTypeDefineRequestDto requestBody = new CollectorTypeDefineRequestDto();
		requestBody.setVersion(CollectorType.DEFINITION_VERSION);

		List<CollectorTypeDefineRequestDto.CollectorTypeDefine> collectorTypes = new ArrayList<>();
		for (CollectorType type : CollectorType.values()) {
			CollectorTypeDefineRequestDto.CollectorTypeDefine collectorType = new CollectorTypeDefineRequestDto.CollectorTypeDefine();
			collectorType.setType(type.name());
			collectorType.setDisplayName(type.getDisplayName());
			collectorType.setArguments(convertArguments(type.getArguments()));

			collectorTypes.add(collectorType);
		}
		requestBody.setCollectorTypes(collectorTypes);
		return requestBody;
	}

	private List<CollectorTypeDefineRequestDto.Argument> convertArguments(List<CollectorArgument> arguments) {
		return arguments.stream()
						.map(it -> {
							CollectorTypeDefineRequestDto.Argument argument = new CollectorTypeDefineRequestDto.Argument();
							argument.setKey(it.getKey());
							argument.setType(it.getType());
							return argument;
						})
						.toList();
	}

	private String toJsonString(CollectorTypeDefineRequestDto requestBody) {
		try {
			return objectMapper.writeValueAsString(requestBody);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void requestConfig(
		String configServerAddress,
		String configName
	) {
		String configApiUrl = configServerAddress + "/api/config/collector?name=" + configName;
		HttpRequest request = HttpRequest.newBuilder()
										 .uri(URI.create(configApiUrl))
										 .header("Authorization",
											 String.format("Bearer %s", portalProperties.getApiToken()))
										 .GET()
										 .build();
		HttpClient client = HttpClient.newHttpClient();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			TypeReference<List<DataCollectPlan>> typeReference = new TypeReference<>() {
			};
			config = objectMapper.readValue(response.body(), typeReference);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (config.isEmpty()) {
			throw new IllegalStateException("Config 정보가 없습니다");
		}
	}

	@Override
	public List<DataCollectPlan> getConfig() {
		return config;
	}

	@Override
	public List<DataCollectPlan> getConfigByToolId(Long toolId) {
		requestConfig(applicationConfig.getConfigServerAddress(), applicationConfig.getConfigName());
		return config;
	}
}
