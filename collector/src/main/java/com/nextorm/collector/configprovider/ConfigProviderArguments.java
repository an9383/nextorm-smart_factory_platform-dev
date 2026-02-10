package com.nextorm.collector.configprovider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;

@Getter
@Slf4j
public class ConfigProviderArguments {
	private static final String CONFIG_PROVIDER_TYPE_ARG_NAME = "config-provider-type";
	private static final String CONFIG_SERVER_ADDRESS_ARG_NAME = "config-server-address";
	private static final String CONFIG_NAME_ARG_NAME = "config-name";

	private final ConfigProviderType configProviderType;

	private String configName;
	private String configServerAddress;

	public ConfigProviderArguments(ApplicationArguments arguments) {
		this.configProviderType = parseConfigProviderType(arguments, ConfigProviderType.DB);

		validateArguments(arguments);
		parseArguments(arguments);

		log.info("Args configProviderType:{}", configProviderType);
		log.info("Args configName:{}", configName);
		log.info("Args configServerAddress:{}", configServerAddress);
	}

	private ConfigProviderType parseConfigProviderType(
		ApplicationArguments arguments,
		ConfigProviderType defaultType
	) {
		boolean existType = arguments.containsOption(CONFIG_PROVIDER_TYPE_ARG_NAME);

		String configProviderType = "";
		if (existType) {
			boolean existTypeValue = !arguments.getOptionValues(CONFIG_PROVIDER_TYPE_ARG_NAME)
											   .isEmpty();
			if (existTypeValue) {
				configProviderType = arguments.getOptionValues(CONFIG_PROVIDER_TYPE_ARG_NAME)
											  .get(0);
			}
		}

		if (existType && !configProviderType.isBlank()) {
			return ConfigProviderType.valueOf(configProviderType.toUpperCase());
		}
		return defaultType;
	}

	private void validateArguments(ApplicationArguments arguments) {
		if (!arguments.containsOption(CONFIG_NAME_ARG_NAME)) {
			throw new IllegalStateException("There is no " + CONFIG_NAME_ARG_NAME);
		}

		if (configProviderType == ConfigProviderType.HTTP && !arguments.containsOption(CONFIG_SERVER_ADDRESS_ARG_NAME)) {
			throw new IllegalStateException("There is no " + CONFIG_SERVER_ADDRESS_ARG_NAME);
		}
	}

	private void parseArguments(ApplicationArguments arguments) {
		this.configName = arguments.getOptionValues(CONFIG_NAME_ARG_NAME)
								   .get(0);

		if (configProviderType == ConfigProviderType.HTTP) {
			this.configServerAddress = arguments.getOptionValues(CONFIG_SERVER_ADDRESS_ARG_NAME)
												.get(0);
		}

	}

	public enum ConfigProviderType {
		HTTP, DB
	}
}
