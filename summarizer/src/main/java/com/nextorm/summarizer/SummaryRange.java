package com.nextorm.summarizer;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * SummaryData의 기간 및 타입정보를 생성, 제공하는 클래스
 */
@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SummaryRange {
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private SummaryPeriodType summaryType;

	/**
	 * baseDateTime을 기준으로 가장 가까운 과거의 섬머리 범위를 생성합니다.
	 */
	public static SummaryRange createNearestPastSummaryRange(
		LocalDateTime baseDateTime,
		SummaryPeriodType summaryType
	) {
		LocalDateTime endTime = calculateNearestPastEndTime(baseDateTime, summaryType);
		LocalDateTime startTime = calculateNearestPastStartTime(endTime, summaryType);

		return new SummaryRange(startTime, endTime, summaryType);
	}

	/**
	 * baseDateTime을 포함하는 섬머리 범위를 생성
	 */
	public static SummaryRange createBaseTimeIncludeSummaryRange(
		LocalDateTime baseDateTime,
		SummaryPeriodType summaryType
	) {
		LocalDateTime start = calculateBaseTimeIncludeStartTime(baseDateTime, summaryType);
		LocalDateTime end = switch (summaryType) {
			case ONE_MINUTE -> start.plusMinutes(1L);
			case TEN_MINUTES -> start.plusMinutes(10L);
			case HOURLY -> start.plusHours(1L);
			case SIX_HOURLIES -> start.plusHours(6L);
			case DAILY -> start.plusDays(1L);
		};

		return new SummaryRange(start, end, summaryType);
	}

	private static LocalDateTime calculateBaseTimeIncludeStartTime(
		LocalDateTime baseDateTime,
		SummaryPeriodType summaryType
	) {
		return switch (summaryType) {
			case ONE_MINUTE -> LocalDateTime.of(baseDateTime.toLocalDate(),
				LocalTime.of(baseDateTime.getHour(), baseDateTime.getMinute()));
			case TEN_MINUTES -> LocalDateTime.of(baseDateTime.toLocalDate(), calculate10MinutesUnit(baseDateTime));
			case HOURLY -> LocalDateTime.of(baseDateTime.toLocalDate(), LocalTime.of(baseDateTime.getHour(), 0));
			case SIX_HOURLIES -> LocalDateTime.of(baseDateTime.toLocalDate(), calculate6HoursUnit(baseDateTime));
			case DAILY -> LocalDateTime.of(baseDateTime.toLocalDate(), LocalTime.MIN);
		};
	}

	private static LocalDateTime calculateNearestPastEndTime(
		LocalDateTime baseDateTime,
		SummaryPeriodType summaryType
	) {
		// baseDateTime 기준 최근의 섬머리 대상 범위의 끝점은 baseDateTime을 포함한 섬머리 범위의 시작점과 같다.
		return calculateBaseTimeIncludeStartTime(baseDateTime, summaryType);
	}

	private static LocalTime calculate10MinutesUnit(LocalDateTime baseDateTime) {
		int currentMinutes = baseDateTime.getMinute();
		int remainder = currentMinutes % 10;
		int minutes = currentMinutes - remainder;
		return LocalTime.of(baseDateTime.getHour(), minutes);
	}

	private static LocalTime calculate6HoursUnit(LocalDateTime baseDateTime) {
		int currentHour = baseDateTime.getHour();
		int remainder = currentHour % 6;
		int hours = currentHour - remainder;
		return LocalTime.of(hours, 0);
	}

	private static LocalDateTime calculateNearestPastStartTime(
		LocalDateTime toDateTime,
		SummaryPeriodType summaryType
	) {
		return switch (summaryType) {
			case ONE_MINUTE -> toDateTime.minusMinutes(1L);
			case TEN_MINUTES -> toDateTime.minusMinutes(10L);

			case HOURLY -> toDateTime.minusHours(1L);
			case SIX_HOURLIES -> toDateTime.minusHours(6L);

			case DAILY -> toDateTime.minusDays(1L);
		};
	}
}
