package com.nextorm.portal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nextorm.ai-server")
public class AiServerProperties {
	private String siteId;
	private String url;
}
