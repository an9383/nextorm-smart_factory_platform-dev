package com.nextorm.extensions.scheduler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.extensions.scheduler.task.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이 클래스 내부에서 사용되는 용어의 의미
 * - 태스크(Task): 스케줄링 가능한 작업 단위
 * - 내부 설정: application.yml 또는 application.properties 파일에 정의된 태스크 설정
 * - 외부 설정: 별도의 JSON 파일로 정의된 태스크 설정
 */
@DisplayName("TaskProperties 클래스 테스트")
class TaskPropertiesTest {

	private TaskProperties taskProperties;
	private ObjectMapper objectMapper;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		taskProperties = new TaskProperties();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("외부 설정 파일이 없을 때 내부 태스크 목록을 반환한다")
	void testGetTasks_WhenTasksConfigLocationIsNull_ShouldReturnInternalTasks() {
		// Given
		Map<String, TaskProperties.Task> internalTasks = taskProperties.getTasks();
		TaskProperties.Task task = new TaskProperties.Task();
		task.setCron("0 0 * * * *");
		task.setType(TaskType.MODBUS_COIL_PULSE);
		internalTasks.put("testTask", task);

		// When
		Map<String, TaskProperties.Task> result = taskProperties.getTasks();

		// Then
		assertThat(result).isNotEmpty();
		assertThat(result).containsKey("testTask");
		assertThat(result.get("testTask")
						 .getCron()).isEqualTo("0 0 * * * *");
		assertThat(result.get("testTask")
						 .getType()).isEqualTo(TaskType.MODBUS_COIL_PULSE);
	}

	@Test
	@DisplayName("외부 설정 파일이 제공될 때 태스크를 파싱하고 병합한다")
	void testGetTasks_WhenTasksConfigLocationIsProvided_ShouldParseAndMergeTasks() throws IOException {
		// Given
		Map<String, Object> configTasks = new HashMap<>();
		Map<String, Object> taskConfig = new HashMap<>();
		taskConfig.put("cron", "0 */5 * * * *");
		taskConfig.put("type", "MODBUS_COIL_PULSE");
		Map<String, Object> properties = new HashMap<>();
		properties.put("host", "localhost");
		properties.put("port", 502);
		taskConfig.put("properties", properties);
		configTasks.put("externalTask", taskConfig);

		File configFile = tempDir.resolve("tasks.json")
								 .toFile();
		objectMapper.writeValue(configFile, configTasks);

		taskProperties.setTasksConfigLocation(new FileSystemResource(configFile));

		// When
		Map<String, TaskProperties.Task> result = taskProperties.getTasks();

		// Then
		assertThat(result).containsKey("externalTask");
		TaskProperties.Task externalTask = result.get("externalTask");
		assertThat(externalTask.getCron()).isEqualTo("0 */5 * * * *");
		assertThat(externalTask.getType()).isEqualTo(TaskType.MODBUS_COIL_PULSE);
		assertThat(externalTask.getProperties()).containsEntry("host", "localhost");
		assertThat(externalTask.getProperties()).containsEntry("port", 502);
	}

	@Test
	@DisplayName("유효하지 않은 설정 파일일 때 RuntimeException을 발생시킨다")
	void testGetTasks_WhenInvalidConfigFile_ShouldThrowRuntimeException() throws IOException {
		// Given
		File invalidConfigFile = tempDir.resolve("invalid.json")
										.toFile();
		Files.write(invalidConfigFile.toPath(), "invalid json content".getBytes());

		taskProperties.setTasksConfigLocation(new FileSystemResource(invalidConfigFile));

		// When & Then
		assertThatThrownBy(() -> taskProperties.getTasks()).isInstanceOf(RuntimeException.class)
														   .hasMessage("TasksConfigLocation 파싱에 실패하였습니다.");
	}

	@Test
	@DisplayName("존재하지 않는 설정 파일일 때 RuntimeException을 발생시킨다")
	void testGetTasks_WhenConfigFileNotExists_ShouldThrowRuntimeException() {
		// Given
		File nonExistentFile = new File(tempDir.toFile(), "nonexistent.json");
		taskProperties.setTasksConfigLocation(new FileSystemResource(nonExistentFile));

		// When & Then
		assertThatThrownBy(() -> taskProperties.getTasks()).isInstanceOf(RuntimeException.class)
														   .hasMessage("TasksConfigLocation 파싱에 실패하였습니다.");
	}

	@Test
	@DisplayName("태스크 설정 파일 위치를 설정할 수 있다")
	void testSetTasksConfigLocation() {
		// Given
		File configFile = new File("test.json");
		FileSystemResource resource = new FileSystemResource(configFile);

		// When
		taskProperties.setTasksConfigLocation(resource);

		// Then
		assertThat(taskProperties.getTasksConfigLocation()).isEqualTo(resource);
	}

	@Test
	@DisplayName("내부 태스크와 외부 태스크가 올바르게 병합된다")
	void testGetTasks_MultipleCalls_ShouldMergeProperly() throws IOException {
		// Given
		// 내부설정 태스크 생성
		Map<String, TaskProperties.Task> internalTasks = taskProperties.getTasks();
		TaskProperties.Task internalTask = new TaskProperties.Task();
		internalTask.setCron("0 0 * * * *");
		internalTask.setType(TaskType.MODBUS_COIL_PULSE);
		internalTasks.put("internalTask", internalTask);

		// 외부설정 파일 생성
		Map<String, Object> configTasks = new HashMap<>();
		Map<String, Object> taskConfig = new HashMap<>();
		taskConfig.put("cron", "0 */10 * * * *");
		taskConfig.put("type", "MODBUS_COIL_PULSE");
		configTasks.put("externalTask", taskConfig);

		File configFile = tempDir.resolve("tasks.json")
								 .toFile();
		objectMapper.writeValue(configFile, configTasks);
		taskProperties.setTasksConfigLocation(new FileSystemResource(configFile));

		// When
		Map<String, TaskProperties.Task> result = taskProperties.getTasks();

		// Then
		assertThat(result).hasSize(2);
		assertThat(result).containsKey("internalTask");
		assertThat(result).containsKey("externalTask");
		assertThat(result.get("internalTask")
						 .getCron()).isEqualTo("0 0 * * * *");
		assertThat(result.get("externalTask")
						 .getCron()).isEqualTo("0 */10 * * * *");
	}
}