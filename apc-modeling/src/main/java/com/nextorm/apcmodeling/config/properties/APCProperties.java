package com.nextorm.apcmodeling.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "apc")
public class APCProperties {
	private String simulationUrl;
}