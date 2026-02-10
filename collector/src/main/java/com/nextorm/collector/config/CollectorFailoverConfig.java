package com.nextorm.collector.config;

import com.nextorm.common.db.entity.CollectorConfig;
import com.nextorm.common.db.repository.CollectorConfigRepository;
import com.nextorm.failover.config.FailoverConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class CollectorFailoverConfig {
	private final ApplicationArguments applicationArguments;
	private final CollectorConfigRepository collectorConfigRepository;

	@Bean
	public FailoverConfig failoverConfig() {
		String collectorConfigName = getCollectorConfigName();
		CollectorConfig collectorConfig = collectorConfigRepository.findOneByName(collectorConfigName)
																   .orElseThrow(() -> new IllegalArgumentException(
																	   "Collector config not found for name: " + collectorConfigName));

		String systemIp = collectorConfig.getSystemIp();
		String zookeeperHosts = collectorConfig.getHosts();
		Integer zookeeperConnectionTimeout = Optional.ofNullable(collectorConfig.getConnectionTimeout())
													 .orElse(30000);

		return FailoverConfig.builder()
							 .systemIp(systemIp)
							 .processName(collectorConfigName)
							 .systemType(FailoverConfig.systemType.collector.name())
							 .zookeeperHosts(zookeeperHosts)
							 .isUse(Boolean.TRUE.equals(collectorConfig.isUseFailover()))
							 .zookeeperConnectionTimeout(zookeeperConnectionTimeout)
							 .build();
	}

	private String getCollectorConfigName() {
		return Optional.ofNullable(applicationArguments.getOptionValues("config-name"))
					   .filter(options -> !options.isEmpty())
					   .map(options -> options.get(0))
					   .orElseThrow(() -> new IllegalArgumentException("Collector config name is required"));
	}
}
