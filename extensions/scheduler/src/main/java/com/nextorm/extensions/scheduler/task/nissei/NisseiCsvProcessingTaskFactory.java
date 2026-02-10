package com.nextorm.extensions.scheduler.task.nissei;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NisseiCsvProcessingTaskFactory {

	private final JdbcTemplate jdbcTemplate;

	public NisseiCsvProcessingTask createTask(NisseiCsvProcessingTaskProperties properties) {
		FileSystemService fileSystemService = new FileSystemServiceImpl();
		CsvProcessor csvProcessor = new CsvProcessorImpl();
		TraceDataRepository traceDataRepository = new TraceDataRepositoryImpl(jdbcTemplate);

		return new NisseiCsvProcessingTask(properties, fileSystemService, csvProcessor, traceDataRepository);
	}
}
