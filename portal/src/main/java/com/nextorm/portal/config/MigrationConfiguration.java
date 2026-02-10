package com.nextorm.portal.config;

import com.nextorm.common.db.repository.HealthSummaryDataRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.SummaryDataRepository;
import com.nextorm.processor.scriptengine.ScriptEngineFactory;
import com.nextorm.summarizer.service.summaryprocessor.SummaryProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MigrationConfiguration {
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterRepository parameterRepository;
	private final SummaryDataRepository summaryDataRepository;
	private final HealthSummaryDataRepository healthSummaryDataRepository;

	@Bean
	public SummaryProcessor summaryProcessor() {
		return new SummaryProcessor(parameterDataRepository,
			summaryDataRepository,
			healthSummaryDataRepository,
			parameterRepository);
	}

	@Bean
	public ScriptEngineFactory scriptEngineFactory(ApplicationContext context) {
		return new ScriptEngineFactory(context);
	}
}
