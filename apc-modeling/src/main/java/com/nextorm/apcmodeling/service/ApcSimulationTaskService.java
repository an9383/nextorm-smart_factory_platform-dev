package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.config.properties.APCProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApcSimulationTaskService {

	private final ApcSimulationService apcSimulationService;
	private final ApcSimulationDataService apcSimulationDataService;
	private final APCProperties apcProperties;
	private final WebClient.Builder webClientBuilder;
	private final ConcurrentHashMap<Long, CompletableFuture<Void>> futureMap = new ConcurrentHashMap<>();

	public void runTask(
		Long apcSimulationId,
		List<Long> apcModelSimulationDataIds
	) {

		CompletableFuture<Void> future = CompletableFuture.runAsync(simulationTask(apcSimulationId,
															  apcModelSimulationDataIds))
														  .whenComplete((result, ex) -> {
															  apcSimulationService.completeApcSimulation(apcSimulationId);
															  futureMap.remove(apcSimulationId);
														  });
		futureMap.put(apcSimulationId, future);
	}

	public Runnable simulationTask(
		Long apcSimulationId,
		List<Long> apcModelSimulationDataIds
	) {
		return () -> {
			try {
				Thread.sleep(100L);
				for (Long simulationDataId : apcModelSimulationDataIds) {
					if (isCanceled(apcSimulationId)) {
						break;
					}
					//시뮬레이션 Data 상태 변경
					setSimulationStatusToSent(simulationDataId);
					//REST API 요청
					requestToApcProcessor(simulationDataId);
				}
			} catch (Exception e) {
				Thread.currentThread()
					  .interrupt(); // 예외 처리
			}
		};
	}

	public void cancelTask(Long apcSimulationId) {
		CompletableFuture<Void> future = futureMap.get(apcSimulationId);
		if (future != null && !future.isDone()) {
			future.cancel(true);
			futureMap.remove(apcSimulationId);
		}
	}

	private boolean isCanceled(Long apcSimulationId) {
		CompletableFuture<Void> future = futureMap.get(apcSimulationId);
		return future == null || future.isCancelled();
	}

	private void setSimulationStatusToSent(Long apcSimulationDataId) {
		apcSimulationDataService.sentApcSimulationData(apcSimulationDataId);
	}

	private void requestToApcProcessor(Long apcModelSimulationDataId) {
		WebClient webClient = webClientBuilder.baseUrl(apcProperties.getSimulationUrl())
											  .build();
		webClient.post()
				 .uri("/" + apcModelSimulationDataId)
				 .exchangeToMono(Mono::just)
				 .block();
	}
}
