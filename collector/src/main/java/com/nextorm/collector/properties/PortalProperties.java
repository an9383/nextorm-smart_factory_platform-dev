package com.nextorm.collector.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "portal")
public class PortalProperties {
	private String apiToken;
}
