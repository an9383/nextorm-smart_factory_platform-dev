package com.nextorm.collector.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.collector.Collector;
import com.nextorm.collector.collector.CollectorFactory;
import com.nextorm.collector.collector.CollectorType;
import com.nextorm.collector.configprovider.ConfigProvider;
import com.nextorm.collector.sender.KafkaSender;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CollectTaskManager {
	private final ObjectMapper objectMapper;
	private final ConfigProvider configProvider;
	private final List<DataCollectPlan> collectorConfigs;
	private final CollectorFactory collectorFactory;
	private final RedisTemplate<String, Object> redisTemplate;

	private final Map<Thread, Collector> collectorMap = new HashMap<>();
	private final ScheduledExecutorService scheduledExecutorService;

	public CollectTaskManager(
		ObjectMapper objectMapper,
		ConfigProvider configProvider,
		CollectorFactory collectorFactory,
		RedisTemplate<String, Object> redisTemplate
	) {
		this.objectMapper = objectMapper;
		this.configProvider = configProvider;
		this.collectorConfigs = configProvider.getConfig();
		this.collectorFactory = collectorFactory;
		this.redisTemplate = redisTemplate;

		this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	}

	public void executeTasks() {
		for (DataCollectPlan config : collectorConfigs) {
			createAndRegisterCollector(config);
		}

		scheduledExecutorService.execute(() -> {
			List<String> threadNames = collectorMap.keySet()
												   .stream()
												   .map(Thread::getName)
												   .toList();

			Map<String, Object> message = new HashMap<>();
			message.put("type", "START");
			message.put("threadNames", threadNames);
			sendEvent(message);
		});

		scheduledExecutorService.execute(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			for (Thread collectorThread : collectorMap.keySet()) {
				collectorThread.start();
			}
		});

		scheduledExecutorService.execute(() -> {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			sendThreadStatusEvent();
			scheduledExecutorService.scheduleWithFixedDelay(this::sendThreadStatusEventIfExistDeadThread,
				5,
				5,
				TimeUnit.MINUTES);
		});

		scheduledExecutorService.scheduleAtFixedRate(this::sendThreadStatusEvent, 1, 1, TimeUnit.HOURS);
	}

	private Sender createSender(
		String bootstrapServer,
		String topic
	) {
		return new KafkaSender(objectMapper, bootstrapServer, topic);
	}

	private Thread createCollectorThread(
		Collector collector,
		DataCollectPlan config
	) {
		Thread thread = new Thread(collector);
		thread.setName("DCP_" + config.getDcpId());
		thread.setUncaughtExceptionHandler((t, e) -> {
			log.error("스레드 내부에서 처리되지 않은 에러가 발생. 쓰레드를 종료합니다: {}", t.getName(), e);
			Map<String, Object> message = new HashMap<>();
			message.put("type", "THREAD_DEAD");
			message.put("threadName", t.getName());
			sendEvent(message);
		});
		return thread;
	}

	private void sendThreadStatusEventIfExistDeadThread() {
		synchronized (collectorMap) {
			Optional<Thread> deadThread = collectorMap.keySet()
													  .stream()
													  .filter(it -> !it.isAlive())
													  .findFirst();

			if (deadThread.isPresent()) {
				sendThreadStatusEvent();
			}
		}

	}

	private void sendThreadStatusEvent() {
		synchronized (collectorMap) {
			List<Map<String, Object>> threads = collectorMap.keySet()
															.stream()
															.map(it -> {
																Map<String, Object> info = new HashMap<>();
																info.put("threadName", it.getName());
																info.put("isAlive", it.isAlive());
																return info;
															})
															.toList();

			Map<String, Object> message = new HashMap<>();
			message.put("type", "STATUS");
			message.put("threads", threads);
			sendEvent(message);
		}
	}

	public void stopTasks() {
		synchronized (collectorMap) {
			for (Map.Entry<Thread, Collector> entry : collectorMap.entrySet()) {
				log.info("컬렉터 종료 프로세스 시작: {}",
					entry.getKey()
						 .getName());
				entry.getValue()
					 .shutdown();

				entry.getKey()
					 .interrupt();
			}
		}
	}

	private void sendEvent(
		Map<String, Object> message
	) {
		try {
			String jsonString = objectMapper.writeValueAsString(message);
			redisTemplate.convertAndSend("collectorEvent", jsonString);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void refreshCollectorByToolId(Long toolId) {
		shutdownToolCollectors(toolId);
		configProvider.getConfigByToolId(toolId)
					  .forEach(config -> {
						  Thread newThread = createAndRegisterCollector(config);
						  newThread.start();
					  });
		log.info("ToolId={} 에 대한 컬렉터 리프레시 완료", toolId);
	}

	private Thread createAndRegisterCollector(DataCollectPlan config) {
		CollectorType type = CollectorType.valueOf(config.getCollectorType());
		Sender kafkaSender = createSender(config.getBootstrapServer(), config.getTopic());

		Collector collector = collectorFactory.createCollector(type, config, Map.of(Sender.class, kafkaSender));

		Thread thread = createCollectorThread(collector, config);
		collectorMap.put(thread, collector);
		return thread;
	}

	private void shutdownToolCollectors(Long toolId) {
		List<Map.Entry<Thread, Collector>> toolCollectors = collectorMap.entrySet()
																		.stream()
																		.filter(entry -> entry.getValue()
																							  .getConfig()
																							  .getToolId()
																							  .equals(toolId))
																		.toList();

		toolCollectors.forEach(entry -> {
			Collector collector = entry.getValue();
			Thread thread = entry.getKey();
			collector.shutdown();
			thread.interrupt();
			collectorMap.remove(thread);
		});
	}
}
