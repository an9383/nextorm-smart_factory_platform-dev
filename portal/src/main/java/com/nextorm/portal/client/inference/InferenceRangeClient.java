package com.nextorm.portal.client.inference;

import com.nextorm.portal.config.properties.AiServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class InferenceRangeClient {
	private static final int MAX_RESPONSE_BODY_SIZE = 20 * 1024 * 1024; // 20MB

	private final AiServerProperties aiServerProperties;
	private final WebClient webClient;

	public InferenceRangeClient(
		AiServerProperties aiServerProperties
	) {
		this.aiServerProperties = aiServerProperties;
        this.aiServerProperties.setUrl("http://localhost:5000");
        log.info(this.aiServerProperties.getUrl());
		this.webClient = WebClient.builder()
								  .exchangeStrategies(ExchangeStrategies.builder()
																		.codecs(configurer -> configurer.defaultCodecs()
																										.maxInMemorySize(
																											MAX_RESPONSE_BODY_SIZE))
																		.build())
								  .baseUrl(aiServerProperties.getUrl())
								  .build();
	}

	public InferenceRangeResponse requestInference(
		Long modelId,
		LocalDateTime from,
		LocalDateTime to
	) {

		InferenceRangeRequest requestBody = InferenceRangeRequest.builder()
																 .site(aiServerProperties.getSiteId())
																 .modelId(modelId)
																 .fromDate(from)
																 .toDate(to)
																 .build();

		return webClient.post()
						.uri("/api/linear-regression/inference-range")
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(requestBody)
						.exchangeToMono(clientResponse -> clientResponse.bodyToMono(InferenceRangeResponse.class))
						.onErrorResume(throwable -> {
							log.error("", throwable);
							return Mono.just(InferenceRangeResponse.failure(throwable.getMessage()));
						})
						.block();
	}
}
