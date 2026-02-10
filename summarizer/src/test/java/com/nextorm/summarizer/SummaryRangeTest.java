package com.nextorm.summarizer;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("섬머리 범위 계산 테스트")
@Slf4j
class SummaryRangeTest {

	@DisplayName("baseTime이 포함된 섬머리 범위 생성")
	@Nested
	class BaseTimeIncludeTest {
		@DisplayName("1분 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:00:00", "2000-01-01T00:00:30", "2000-01-01T00:00:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_minutes(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.ONE_MINUTE;

			// when
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 0, 1, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("10분 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:00:00", "2000-01-01T00:05:30", "2000-01-01T00:09:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_ten_minutes(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.TEN_MINUTES;

			// when
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 0, 10, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("1시간 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:00:00", "2000-01-01T00:30:30", "2000-01-01T00:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_hours(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.HOURLY;

			// when
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 1, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("6시간 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:00:00", "2000-01-01T03:30:30", "2000-01-01T05:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_six_hours(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.SIX_HOURLIES;

			// when
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 6, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("1일 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:00:00", "2000-01-01T12:30:30", "2000-01-01T23:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_daily(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.DAILY;

			// when
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 2, 0, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}
	}

	@DisplayName("baseTime에서 가장 가까운 과거 섬머리 범위 생성")
	@Nested
	class NearestPastTest {
		@DisplayName("1분 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:01:00", "2000-01-01T00:01:30", "2000-01-01T00:01:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_minutes(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.ONE_MINUTE;

			// when
			SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 0, 1, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("10분 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T00:10:00", "2000-01-01T00:15:30", "2000-01-01T00:19:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_ten_minutes(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.TEN_MINUTES;

			// when
			SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 0, 10, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("1시간 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T01:00:00", "2000-01-01T01:30:30", "2000-01-01T01:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_hours(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.HOURLY;

			// when
			SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 1, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("6시간 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-01T06:00:00", "2000-01-01T09:30:30", "2000-01-01T11:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_six_hours(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.SIX_HOURLIES;

			// when
			SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 1, 6, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}

		@DisplayName("1일 섬머리 범위 생성")
		@ParameterizedTest(name = "baseDateTime: {0}")
		@ValueSource(strings = {"2000-01-02T00:00:00", "2000-01-02T12:30:30", "2000-01-02T23:59:59.999999999"})
		void createBaseTimeIncludeSummaryRangeTest_one_daily(String baseTimeString) {
			// given
			LocalDateTime base = LocalDateTime.parse(baseTimeString);
			SummaryPeriodType summaryType = SummaryPeriodType.DAILY;

			// when
			SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(base, summaryType);

			// then
			LocalDateTime expectedStartTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LocalDateTime expectedEndTime = LocalDateTime.of(2000, 1, 2, 0, 0, 0, 0);

			assertThat(summaryRange.getStartTime()).isEqualTo(expectedStartTime);
			assertThat(summaryRange.getEndTime()).isEqualTo(expectedEndTime);
		}
	}
}