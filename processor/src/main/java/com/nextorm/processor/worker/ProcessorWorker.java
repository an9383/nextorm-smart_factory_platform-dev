package com.nextorm.processor.worker;

import com.nextorm.processor.service.ProcessedDataService;
import com.nextorm.processor.worker.datacollector.CollectData;
import com.nextorm.processor.worker.datacollector.DataCollector;
import com.nextorm.processor.worker.dataprocessor.CollectDataProcessor;
import com.nextorm.processor.worker.dataprocessor.ProcessCollectDataResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ProcessorWorker implements Runnable {
	private final WorkerConfig workerConfig;
	private final DataCollector dataCollector;
	private final CollectDataProcessor collectDataProcessor;
	private final ProcessedDataService processedDataService;
	private final CountDownLatch latch;

	private final AtomicBoolean isRunning = new AtomicBoolean(true);

	public static ProcessorWorker createWorker(
		WorkerConfig workerConfig,
		DataCollector dataCollector,
		CollectDataProcessor collectDataProcessor,
		ProcessedDataService processedDataService
	) {
		return ProcessorWorker.builder()
							  .workerConfig(workerConfig)
							  .dataCollector(dataCollector)
							  .collectDataProcessor(collectDataProcessor)
							  .processedDataService(processedDataService)
							  .latch(new CountDownLatch(1))
							  .build();
	}

	@Override
	public void run() {
		try {
			init();
			execute();
		} catch (Exception e) {
			String msg = "Worker 실행중 에러가 발생하였습니다. (ToolID: %s, 토픽: %s)".formatted(workerConfig.getToolId(),
				workerConfig.getTopic());
			log.error(msg, e);
		} finally {
			dataCollector.close();
			log.info("Worker is shutting down!");
			latch.countDown();
		}
	}

	private void init() {
		collectDataProcessor.init();
	}

	private void execute() {
		while (isRunning.get()) {

			List<CollectData> collectDataList = dataCollector.collectData();

			long start = System.currentTimeMillis();
			ProcessCollectDataResult processCollectDataResult = collectDataProcessor.processCollectDataList(
				collectDataList);

			processedDataService.saveAndFaultEventPublish(processCollectDataResult.parameterDataList(),
				processCollectDataResult.faultHistoryList());

			dataCollector.commitOffset();

			log.info("[{}ms - records: {}] Save parameter/fault data list size: {} / {}",
				(System.currentTimeMillis() - start),
				collectDataList.size(),
				processCollectDataResult.parameterDataList()
										.size(),
				processCollectDataResult.faultHistoryList()
										.size());
		}
	}

	/**
	 * @return CountDownLatch: shutdown 시 worker가 종료되었음을 확인하기 위한 CountDownLatch. await 메소드를 이용해 종료 여부 확인할 수 있음
	 */
	public CountDownLatch shutdown() {
		this.isRunning.set(false);
		return latch;
	}
}
