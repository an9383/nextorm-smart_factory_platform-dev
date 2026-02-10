package com.nextorm.extensions.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.extensions.scheduler.config.TaskProperties;
import com.nextorm.extensions.scheduler.task.Task;
import com.nextorm.extensions.scheduler.task.TaskType;
import com.nextorm.extensions.scheduler.task.kcs.*;
import com.nextorm.extensions.scheduler.task.modbus.ModbusCoilPulseProperties;
import com.nextorm.extensions.scheduler.task.modbus.ModbusCoilPulseTask;
import com.nextorm.extensions.scheduler.task.modbus.ModbusConnectionFactory;
import com.nextorm.extensions.scheduler.task.modbus.TcpModbusConnectionFactory;
import com.nextorm.extensions.scheduler.task.nissei.NisseiCsvProcessingTaskFactory;
import com.nextorm.extensions.scheduler.task.nissei.NisseiCsvProcessingTaskProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduleManager {
	private final TaskProperties taskProperties;
	private final TaskScheduler taskScheduler;
	private final NisseiCsvProcessingTaskFactory nisseiCsvProcessingTaskFactory;
	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	private final Map<String, TaskInfo> taskInfoMap = new HashMap<>();

	@EventListener(ApplicationReadyEvent.class)
	public void registerSchedule() {
		log.info("스케줄러 등록 시작");
		taskProperties.getTasks()
					  .forEach((taskName, taskProperty) -> {
						  Task task = createTask(taskProperty);
						  CronTrigger cronTrigger = new CronTrigger(taskProperty.getCron());
						  ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task::execute, cronTrigger);
						  TaskInfo taskInfo = new TaskInfo(task, cronTrigger, taskProperty, scheduledFuture);
						  taskInfoMap.put(taskName, taskInfo);
						  log.info(">> 태스크 {} 등록. cron: {}", taskName, cronTrigger);
					  });
		log.info("스케줄러 등록 완료");
	}

	private Task createTask(TaskProperties.Task taskProperty) {
		validType(taskProperty.getType());
		validCron(taskProperty.getCron());

		// 모든 타입에 대해 필수로 구현체를 제공해야 한다
		return switch (taskProperty.getType()) {
			case MODBUS_COIL_PULSE -> {
				ModbusCoilPulseProperties properties = new ModbusCoilPulseProperties(taskProperty.getProperties());
				ModbusConnectionFactory connectionFactory = new TcpModbusConnectionFactory();
				yield new ModbusCoilPulseTask(properties, connectionFactory);
			}
			case NISSEI_CSV_READER -> {
				NisseiCsvProcessingTaskProperties properties = new NisseiCsvProcessingTaskProperties(taskProperty.getProperties());
				yield nisseiCsvProcessingTaskFactory.createTask(properties);
			}
			case KCS -> {
				// EQMS는 별도 설정으로 DB정보를 주입받으므로, 여기서 DataSource를 생성
				EqmsRepositoryProperties eqmsRepositoryProperties = new EqmsRepositoryProperties(taskProperty.getProperties());
				DataSource eqmsDataSource = DataSourceFactory.createDataSource(eqmsRepositoryProperties.getJdbcUrl(),
					eqmsRepositoryProperties.getUser(),
					eqmsRepositoryProperties.getPassword(),
					eqmsRepositoryProperties.getJdbcDriverClassName());
				EqmsRepository eqmsRepository = new EqmsRepository(new JdbcTemplate(eqmsDataSource));

				SfpRepository sfpRepository = new SfpRepository(jdbcTemplate);

				yield new KcsTask(sfpRepository, eqmsRepository, objectMapper);
			}
		};
	}

	private void validType(TaskType type) {
		if (type == null) {
			throw new IllegalArgumentException("task의 타입은 필수입니다. 허용 타입: " + List.of(TaskType.values()));
		}
	}

	private void validCron(String cron) {
		try {
			new CronTrigger(cron);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("cron 표현식이 잘못되었습니다: " + cron, e);
		}
	}

	record TaskInfo(Task task, CronTrigger cron, TaskProperties.Task property, ScheduledFuture<?> scheduledFuture) {
	}
}
