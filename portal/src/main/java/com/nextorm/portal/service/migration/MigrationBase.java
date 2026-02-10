package com.nextorm.portal.service.migration;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MigrationBase {
	private LocalDateTime traceAt;
	private Double latitude;
	private Double longitude;

	public MigrationBase(LocalDateTime traceAt) {
		this.traceAt = traceAt;
	}

	public MigrationBase(
		LocalDateTime traceAt,
		Double latitude,
		Double longitude
	) {
		this.traceAt = traceAt;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
