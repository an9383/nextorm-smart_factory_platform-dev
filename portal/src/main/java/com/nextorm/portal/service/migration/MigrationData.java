package com.nextorm.portal.service.migration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MigrationData implements Comparable<MigrationData> {
	private Double latitude;
	private Double longitude;
	private String value;
	private LocalDateTime traceAt;

	public static MigrationData of(
		String value,
		LocalDateTime traceAt,
		Double latitude,
		Double longitude
	) {
		MigrationData migrationData = new MigrationData();
		migrationData.traceAt = traceAt;
		migrationData.value = value;
		migrationData.latitude = latitude;
		migrationData.longitude = longitude;
		return migrationData;
	}

	@Override
	public int compareTo(@NotNull MigrationData another) {
		return this.traceAt.compareTo(another.traceAt);
	}

	public MigrationBase toMigrationBase() {
		return new MigrationBase(traceAt, latitude, longitude);
	}
}
