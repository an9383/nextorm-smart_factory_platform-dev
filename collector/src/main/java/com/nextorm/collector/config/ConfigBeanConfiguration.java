package com.nextorm.collector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.configprovider.ConfigProvider;
import com.nextorm.collector.configprovider.ConfigProviderArguments;
import com.nextorm.collector.configprovider.DBConfigProvider;
import com.nextorm.collector.configprovider.HttpConfigProvider;
import com.nextorm.collector.properties.PortalProperties;
import com.nextorm.common.db.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.Properties;

@Configuration
public class ConfigBeanConfiguration {
	@Bean
	public ConfigProviderArguments collectorRequiredArgumentsConfig(ApplicationArguments arguments) {
		return new ConfigProviderArguments(arguments);
	}

	@Bean
	public ConfigProvider configProvider(
		ConfigProviderArguments configProviderArguments,
		ObjectMapper objectMapper,
		PortalProperties portalProperties,
		PlatformTransactionManager transactionManager,
		CollectorDefineRepository collectorDefineRepository,
		DcpConfigRepository dcpConfigRepository,
		CollectorConfigRepository collectorConfigRepository,
		ToolKafkaRepository toolKafkaRepository,
		ParameterExtraDataRepository parameterExtraDataRepository
	) {
		if (configProviderArguments.getConfigProviderType() == ConfigProviderArguments.ConfigProviderType.HTTP) {
			return new HttpConfigProvider(objectMapper, configProviderArguments, portalProperties);
		}
		return createProxyDBConfigProvider(configProviderArguments,
			collectorDefineRepository,
			dcpConfigRepository,
			collectorConfigRepository,
			transactionManager,
			toolKafkaRepository,
			parameterExtraDataRepository);
	}

	private ConfigProvider createProxyDBConfigProvider(
		ConfigProviderArguments configProviderArguments,
		CollectorDefineRepository collectorDefineRepository,
		DcpConfigRepository dcpConfigRepository,
		CollectorConfigRepository collectorConfigRepository,
		PlatformTransactionManager transactionManager,
		ToolKafkaRepository toolKafkaRepository,
		ParameterExtraDataRepository parameterExtraDataRepository
	) {
		TransactionProxyFactoryBean proxy = new TransactionProxyFactoryBean();

		proxy.setTransactionManager(transactionManager);
		proxy.setTarget(new DBConfigProvider(configProviderArguments,
			collectorDefineRepository,
			dcpConfigRepository,
			collectorConfigRepository,
			toolKafkaRepository,
			parameterExtraDataRepository));

		Properties transactionAttributes = new Properties();
		transactionAttributes.put("*", "PROPAGATION_REQUIRED");
		proxy.setTransactionAttributes(transactionAttributes);

		proxy.afterPropertiesSet();
		return (ConfigProvider)proxy.getObject();
	}

}
