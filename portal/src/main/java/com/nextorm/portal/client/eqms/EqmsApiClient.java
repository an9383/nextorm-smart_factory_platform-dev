package com.nextorm.portal.client.eqms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.portal.config.properties.EqmsApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EqmsApiClient {
	private static final int MAX_RESPONSE_BODY_SIZE = 20 * 1024 * 1024; // 20MB

	private final EqmsApiProperties eqmsApiProperties;
	private final ObjectMapper objectMapper;
	private final WebClient webClient;

	public EqmsApiClient(
		EqmsApiProperties eqmsApiProperties,
		ObjectMapper objectMapper
	) {
		this.eqmsApiProperties = eqmsApiProperties;
		this.objectMapper = objectMapper;

		this.webClient = WebClient.builder()
								  .exchangeStrategies(ExchangeStrategies.builder()
																		.codecs(configurer -> configurer.defaultCodecs()
																										.maxInMemorySize(
																											MAX_RESPONSE_BODY_SIZE))
																		.build())
								  .baseUrl(eqmsApiProperties.getUrl())
								  .defaultHeader(eqmsApiProperties.getApiKeyHeader(), eqmsApiProperties.getApiKey())
								  .build();
	}

	public EqmsProductsResponse getProducts() {
		return webClient.get()
						.uri("/api/product/products")
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.exchangeToMono(clientResponse -> clientResponse.bodyToMono(List.class))
						.map(items -> {
							List<EqmsProductsResponse.Product> products = items.stream()
																			   .map(it -> {
																				   try {
																					   return convertToProduct((LinkedHashMap<String, Object>)it);
																				   } catch (JsonProcessingException e) {
																					   throw new RuntimeException(e);
																				   }
																			   })
																			   .toList();
							return EqmsProductsResponse.success(products);
						})
						.onErrorResume(throwable -> {
							log.error("", throwable);
							return Mono.just(EqmsProductsResponse.failure(throwable.getMessage()));
						})
						.block();
	}

	private EqmsProductsResponse.Product convertToProduct(LinkedHashMap<String, Object> item) throws
		JsonProcessingException {
		Long id = item.getOrDefault("id", null) != null
				  ? Long.parseLong(item.get("id")
									   .toString())
				  : null;

		String name = item.get("name") != null
					  ? item.get("name")
							.toString()
					  : null;

		String description = item.get("description") != null
							 ? item.get("description")
								   .toString()
							 : null;
		Map<String, Object> ext = objectMapper.readValue((String)item.getOrDefault("ext", "{}"), Map.class);
		return new EqmsProductsResponse.Product(id, name, description, ext);
	}
}
