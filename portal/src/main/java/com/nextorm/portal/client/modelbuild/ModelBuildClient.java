package com.nextorm.portal.client.modelbuild;

import com.nextorm.portal.config.properties.AiServerProperties;
import com.nextorm.portal.service.AiService;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.ConnectException;

@Slf4j
@Service
public class ModelBuildClient {
	private final AiServerProperties aiServerProperties;
	private final AiService aiService;

	private final WebClient webClient;

	public ModelBuildClient(
		AiServerProperties aiServerProperties,
		AiService aiService
	) {
		this.aiServerProperties = aiServerProperties;
        this.aiServerProperties.setUrl("http://localhost:5000");
        log.info("URL: {}", this.aiServerProperties.getUrl());
        this.aiService = aiService;

		this.webClient = WebClient.builder()
								  .baseUrl(aiServerProperties.getUrl())
								  .build();
	}

	public void requestModelTraining(Long modelId) {
		ModelBuildRequest requestBody = ModelBuildRequest.builder()
														 .site(aiServerProperties.getSiteId())
														 .modelId(modelId)
														 .build();

		webClient.post()
				 .uri("/api/linear-regression/build")
				 .contentType(MediaType.APPLICATION_JSON)
				 .bodyValue(requestBody)
				 .exchangeToMono(clientResponse -> clientResponse.bodyToMono(ModelBuildResponse.class))
				 .publishOn(Schedulers.boundedElastic())
				 .doOnNext(modelBuildResponse -> {
					 if (!modelBuildResponse.isSuccess()) {
						 aiService.updateModelFailureStatus(modelId, modelBuildResponse.getMessage());
					 }
				 })
				 .onErrorResume(throwable -> {
					 handleError(throwable, modelId);
					 return Mono.empty();
				 })
				 .subscribe();
	}

	private void handleError(
		Throwable error,
		Long modelId
	) {
		String errorMessage = null;
		Throwable cause = error.getCause();

		if (cause instanceof ReadTimeoutException) {
			return;
		}

		if (cause instanceof ConnectTimeoutException) {
			log.info("모델 학습 요청 연결 타임아웃: {}", error.getMessage());
			errorMessage = "모델 학습 요청 연결 타임아웃";
		}

		if (cause instanceof TimeoutException) {
			log.info("모델 학습 요청 중 타임아웃", error);
			errorMessage = "모델 학습 요청 중 타임아웃";
		}

		if (cause instanceof ConnectException) {
			log.info("모델 학습 요청 연결 실패", error);
			errorMessage = "모델 학습 요청 연결 실패";
		}

		if (errorMessage == null) {
			log.error("모델 학습 요청 실패. 핸들링 되지 않는 에러 발생", error);
			errorMessage = "모델 학습 요청 실패 (로그 확인이 필요합니다)";
		}

		aiService.updateModelFailureStatus(modelId, errorMessage);
	}
}
