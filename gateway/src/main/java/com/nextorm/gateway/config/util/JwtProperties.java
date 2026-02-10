package com.nextorm.gateway.config.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.auth.jwt")
public class JwtProperties {
	private String secretKey;
	private String accessTokenHeaderPrefix;
	private String stomyToken;
}