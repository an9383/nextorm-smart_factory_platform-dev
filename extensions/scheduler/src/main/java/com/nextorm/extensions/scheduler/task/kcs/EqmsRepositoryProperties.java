package com.nextorm.extensions.scheduler.task.kcs;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Map;

@Getter
public class EqmsRepositoryProperties {
	private final String jdbcUrl;
	private final String user;
	private final String password;
	private final String jdbcDriverClassName;

	public EqmsRepositoryProperties(Map<String, Object> properties) {
		this.jdbcUrl = validAndGetProperty("eqmsDbJdbcUrl", properties);
		this.user = validAndGetProperty("eqmsDbUser", properties);
		this.password = validAndGetProperty("eqmsDbPassword", properties);
		this.jdbcDriverClassName = validAndGetProperty("eqmsDbJdbcDriver", properties);
	}

	private String validAndGetProperty(
		String key,
		Map<String, Object> property
	) {
		Object value = property.get(key);
		if (value == null) {
			throw new IllegalArgumentException("필수 속성 누락: " + key);
		}
		if (!StringUtils.hasText(value.toString())) {
			throw new IllegalArgumentException("필수 속성 비어있음: " + key);
		}
		return value.toString();
	}
}
