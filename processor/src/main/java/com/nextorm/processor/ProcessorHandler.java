package com.nextorm.processor;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.scriptengine.ScriptEngineFactory;
import com.nextorm.processor.service.*;
import com.nextorm.processor.worker.ProcessorWorker;
import com.nextorm.processor.worker.WorkerConfig;
import com.nextorm.processor.worker.datacollector.DataCollector;
import com.nextorm.processor.worker.datacollector.KafkaDataCollector;
import com.nextorm.processor.worker.dataprocessor.CollectDataProcessor;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessorHandler {
	private final Map<Long, ProcessorWorker> toolWorkerMap = new HashMap<>();
	private final ParameterContainer parametersContainer;
	private final ToolRepository toolRepository;
	private final HandlerService handlerService;
	private final WorkerService workerService;
	private final AutoSpecCalculator autoSpecCalculator;
	private final ScriptEngineFactory scriptEngineFactory;
	private final ConsumerFactory consumerFactory;
	private final ProcessedDataService processedDataService;

	private String processConfigName;

	public void execute(ApplicationArguments args) {
		if (!args.containsOption("name")) {
			throw new IllegalArgumentException("There is no name!");
		}
		processConfigName = args.getOptionValues("name")
								.get(0);

		List<ProcessingToolInfo> processingToolInfos = handlerService.getProcessingToolInfos(processConfigName);

		for (ProcessingToolInfo processingToolInfo : processingToolInfos) {
			ProcessorWorker worker = createAndStartWorker(processingToolInfo);
			toolWorkerMap.put(processingToolInfo.getToolId(), worker);
		}
	}

	private ProcessorWorker createAndStartWorker(ProcessingToolInfo processingToolInfo) {
		Long toolId = processingToolInfo.getToolId();
		parametersContainer.initParametersByToolId(toolId, processingToolInfo.getDcpIds());

		WorkerConfig workerConfig = WorkerConfig.of(toolId,
			processingToolInfo.getTopic(),
			processingToolInfo.getBootstrapServers());

		DataCollector dataCollector = new KafkaDataCollector(consumerFactory,
			processingToolInfo.getBootstrapServers(),
			processingToolInfo.getTopic());

		CollectDataProcessor collectDataProcessor = new CollectDataProcessor(toolId,
			parametersContainer,
			autoSpecCalculator,
			workerService,
			scriptEngineFactory);

		ProcessorWorker worker = ProcessorWorker.createWorker(workerConfig,
			dataCollector,
			collectDataProcessor,
			processedDataService);

		Thread t = new Thread(worker);
		t.setName("Tool: " + processingToolInfo.getToolName());
		t.start();
		return worker;
	}

	/**
	 * 어플리케이션이 종료되는 시점에 생성되었던 워커들을 종료시킨다.
	 */
	@PreDestroy
	public void close() {
		for (ProcessorWorker workerThread : toolWorkerMap.values()) {
			CountDownLatch latch = workerThread.shutdown();
			try {
				latch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			log.info("DataProcessorWorker is shutting down...");
		}
	}

	public void refreshTool(Long toolId) {
		log.info("refreshing ProcessorWorker for toolId: {}", toolId);
		Tool tool = toolRepository.findById(toolId)
								  .get();
		ProcessorWorker currentWorker = toolWorkerMap.get(toolId);

		if (currentWorker == null) {
			log.warn("ProcessorWorker for toolId: {} not found.", toolId);
			return;
		}

		try {
			CountDownLatch latch = currentWorker.shutdown();
			log.info("{} DataProcessorWorker is shutting down...", tool.getName());
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		toolWorkerMap.remove(toolId);

		List<ProcessingToolInfo> processingToolInfos = handlerService.getProcessingToolInfos(processConfigName);
		ProcessingToolInfo processingToolInfo = processingToolInfos.stream()
																   .filter(v -> v.getToolId()
																				 .equals(toolId))
																   .findFirst()
																   .get();

		ProcessorWorker newWorker = createAndStartWorker(processingToolInfo);
		toolWorkerMap.put(toolId, newWorker);
		log.info("ProcessorWorker for toolId: {} has been refreshed.", toolId);
	}
}
