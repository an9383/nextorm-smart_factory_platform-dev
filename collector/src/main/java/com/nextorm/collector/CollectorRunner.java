package com.nextorm.collector;

import com.nextorm.collector.task.CollectTaskManager;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollectorRunner {
	private final CollectTaskManager taskManager;

	public void executeTasks() {
		taskManager.executeTasks();
	}

	@PreDestroy
	public void destroy() {
		taskManager.stopTasks();
	}
}
