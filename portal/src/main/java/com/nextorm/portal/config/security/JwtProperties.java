package com.nextorm.portal.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "spring.auth.jwt")
public class JwtProperties {
	private String secretKey;
	private int accessTokenExpirationSecs;
	private int refreshTokenExpirationSecs;
	private String accessTokenHeader;
	private String accessTokenHeaderPrefix;
	private String refreshTokenCookieName;
	private String collectorApiToken;
	private List<String> collectorApiUri = List.of();
	private String aidasApiToken;
	private List<String> aidasApiUri = List.of();
}