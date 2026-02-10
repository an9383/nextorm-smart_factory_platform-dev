package com.nextorm.portal.client.processoptimization;

import com.nextorm.portal.config.properties.AiServerProperties;
import com.nextorm.portal.service.processoptimization.ProcessOptimizationService;
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
public class ProcessOptimizationClient {
	private final ProcessOptimizationService processOptimizationService;
	private final AiServerProperties aiServerProperties;
	private final WebClient webClient;

	public ProcessOptimizationClient(
		ProcessOptimizationService processOptimizationService,
		AiServerProperties aiServerProperties
	) {
		this.processOptimizationService = processOptimizationService;
		this.aiServerProperties = aiServerProperties;

		this.webClient = WebClient.builder()
								  .baseUrl(aiServerProperties.getUrl())
								  .build();
	}

	public void requestProcessOptimization(Long optimizationId) {

		ProcessOptimizationRequest requestBody = ProcessOptimizationRequest.builder()
																		   .site(aiServerProperties.getSiteId())
																		   .optimizationId(optimizationId)
																		   .build();

		webClient.post()
				 .uri("/api/linear-regression/simulate-optimal-values")
				 .contentType(MediaType.APPLICATION_JSON)
				 .bodyValue(requestBody)
				 .exchangeToMono(clientResponse -> clientResponse.bodyToMono(ProcessOptimizationResponse.class))
				 .publishOn(Schedulers.boundedElastic())
				 .doOnNext(modelBuildResponse -> {
					 if (!modelBuildResponse.isSuccess()) {
						 processOptimizationService.updateOptimizationFailureStatus(optimizationId,
							 modelBuildResponse.getMessage());
					 }
				 })
				 .onErrorResume(throwable -> {
					 handleError(throwable, optimizationId);
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
			errorMessage = "시뮬레이션 요청 연결 타임타웃";
			log.info("{}: {}", errorMessage, error.getMessage());
		}

		if (cause instanceof TimeoutException) {
			errorMessage = "시뮬레이션 요청 중 타임아웃";
			log.info("{}", errorMessage, error);
		}

		if (cause instanceof ConnectException) {
			errorMessage = "시뮬레이션 요청 연결 실패";
			log.info("{}", errorMessage, error);
		}

		if (errorMessage == null) {
			errorMessage = "시뮬레이션 실패 (로그 확인이 필요합니다)";
			log.error("시뮬레이션 실패. 핸들링 되지 않는 에러 발생", error);
		}

		processOptimizationService.updateOptimizationFailureStatus(modelId, errorMessage);
	}

}
