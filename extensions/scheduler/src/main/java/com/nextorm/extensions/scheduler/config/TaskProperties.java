package com.nextorm.extensions.scheduler.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.extensions.scheduler.task.TaskType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "scheduler")
public class TaskProperties {
	private final Map<String, Task> tasks = new HashMap<>();
	private Resource tasksConfigLocation;

	public Map<String, Task> getTasks() {
		if (tasksConfigLocation == null) {
			return tasks;
		}

		try {
			Map<String, Task> parsed = new ObjectMapper().readValue(tasksConfigLocation.getInputStream(),
				new TypeReference<Map<String, Task>>() {
				});
			tasks.putAll(parsed);
			return tasks;
		} catch (IOException e) {
			throw new RuntimeException("TasksConfigLocation 파싱에 실패하였습니다.", e);
		}
	}

	@Data
	public static class Task {
		private String cron;
		private TaskType type;
		private Map<String, Object> properties = new HashMap<>();
	}

}
