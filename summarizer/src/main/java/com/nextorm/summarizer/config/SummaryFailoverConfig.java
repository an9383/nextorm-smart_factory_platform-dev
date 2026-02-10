package com.nextorm.summarizer.config;

import com.nextorm.common.db.entity.SummaryConfig;
import com.nextorm.common.db.repository.SummaryConfigRepository;
import com.nextorm.failover.config.FailoverConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SummaryFailoverConfig {
	private final ApplicationArguments applicationArguments;
	private final SummaryConfigRepository summaryConfigRepository;

	@Bean
	public FailoverConfig failoverConfig() {
		String summaryConfigName = getSummaryConfigName();
		SummaryConfig summaryConfig = summaryConfigRepository.findOneByName(summaryConfigName)
															 .orElseThrow(() -> new IllegalArgumentException(
																 "Summary config not found for name: " + summaryConfigName));

		String systemIp = summaryConfig.getSystemIp();
		String zookeeperHosts = summaryConfig.getHosts();
		Integer zookeeperConnectionTimeout = Optional.ofNullable(summaryConfig.getConnectionTimeout())
													 .orElse(30000);

		return FailoverConfig.builder()
							 .systemIp(systemIp)
							 .processName(summaryConfigName)
							 .systemType(FailoverConfig.systemType.summarizer.name())
							 .zookeeperHosts(zookeeperHosts)
							 .isUse(Boolean.TRUE.equals(summaryConfig.getIsUseFailover()))
							 .zookeeperConnectionTimeout(zookeeperConnectionTimeout)
							 .build();
	}

	private String getSummaryConfigName() {
		return Optional.ofNullable(applicationArguments.getOptionValues("name"))
					   .filter(options -> !options.isEmpty())
					   .map(options -> options.get(0))
					   .orElseThrow(() -> new IllegalArgumentException("Summary config name is required"));
	}
}
