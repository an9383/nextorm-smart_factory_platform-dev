package com.nextorm.extensions.misillan.alarm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("source.eqms.portal")
public class EqmsPortalSourceConfig {
	private String url;
	private String id;
	private String password;
}
