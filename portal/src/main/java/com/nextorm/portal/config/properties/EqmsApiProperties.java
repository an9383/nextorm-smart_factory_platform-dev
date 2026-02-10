package com.nextorm.portal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nextorm.eqms")
public class EqmsApiProperties {
	private String url;
	private String apiKeyHeader;
	private String apiKey;
}
