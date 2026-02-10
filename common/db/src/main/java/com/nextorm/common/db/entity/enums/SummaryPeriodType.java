package com.nextorm.common.db.entity.enums;

import lombok.Getter;

@Getter
public enum SummaryPeriodType {
	ONE_MINUTE(1, "1분"), TEN_MINUTES(10, "10분"), HOURLY(60, "1시간"), SIX_HOURLIES(360, "6시간"), DAILY(1440, "1일");

	private final long minutes;
	private final String displayName;

	SummaryPeriodType(
		long minutes,
		String displayName
	) {
		this.minutes = minutes;
		this.displayName = displayName;
	}
}
