package com.nextorm.summarizer;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SummaryTimeTracker {
	private int checkMinute;
	private int check10Minutes;
	private int checkHour;
	private int checkSixHour;
	private int checkDay;

	public SummaryTimeTracker() {
		init();
	}

	private void init() {
		LocalDateTime currentTime = LocalDateTime.now();
		for (SummaryPeriodType summaryType : SummaryPeriodType.values()) {
			updateTrackerBySummaryType(currentTime, summaryType);
		}
	}

	public void updateTrackerBySummaryType(
		LocalDateTime baseTime,
		SummaryPeriodType summaryType
	) {
		switch (summaryType) {
			case ONE_MINUTE -> checkMinute = baseTime.getMinute();
			case TEN_MINUTES -> check10Minutes = baseTime.getMinute() - (baseTime.getMinute() % 10);
			case HOURLY -> checkHour = baseTime.getHour();
			case SIX_HOURLIES -> checkSixHour = baseTime.getHour() - (baseTime.getHour() % 6);
			case DAILY -> checkDay = baseTime.getDayOfMonth();
		}
	}
}
