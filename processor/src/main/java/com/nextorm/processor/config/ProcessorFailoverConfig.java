package com.nextorm.processor.config;

import com.nextorm.common.db.entity.ProcessConfig;
import com.nextorm.common.db.repository.ProcessConfigRepository;
import com.nextorm.failover.config.FailoverConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ProcessorFailoverConfig {
	private final ApplicationArguments applicationArguments;
	private final ProcessConfigRepository processConfigRepository;

	@Bean
	public FailoverConfig failoverConfig() {
		String processorConfigName = getProcessorConfigName();
		ProcessConfig processConfig = processConfigRepository.findOneByName(processorConfigName)
															 .orElseThrow(() -> new IllegalArgumentException(
																 "Processor config not found for name: " + processorConfigName));

		String systemIp = processConfig.getSystemIp();
		String zookeeperHosts = processConfig.getHosts();
		Integer zookeeperConnectionTimeout = Optional.ofNullable(processConfig.getConnectionTimeout())
													 .orElse(30000);

		return FailoverConfig.builder()
							 .systemIp(systemIp)
							 .processName(processorConfigName)
							 .systemType(FailoverConfig.systemType.processor.name())
							 .zookeeperHosts(zookeeperHosts)
							 .isUse(Boolean.TRUE.equals(processConfig.getIsUseFailover()))
							 .zookeeperConnectionTimeout(zookeeperConnectionTimeout)
							 .build();
	}

	private String getProcessorConfigName() {
		return Optional.ofNullable(applicationArguments.getOptionValues("name"))
					   .filter(options -> !options.isEmpty())
					   .map(options -> options.get(0))
					   .orElseThrow(() -> new IllegalArgumentException("Process config name is required"));
	}
}
