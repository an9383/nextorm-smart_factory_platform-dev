package com.nextorm.summarizer.service.summaryprocessor;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.SummaryConfigToolMappingRepository;
import com.nextorm.common.db.repository.SummaryDataRepository;
import com.nextorm.summarizer.SummaryRange;
import com.nextorm.summarizer.dto.SummaryDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
@EnableAsync
public class MissedSummaryProcessor {
	private final SummaryDataRepository summaryDataRepository;
	private final SummaryConfigToolMappingRepository summaryConfigToolMappingRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterRepository parameterRepository;
	private final SummaryProcessor summaryProcessor;

	//Summary PeriodType에 따른 시간 값 계산
	private enum SummaryPeriodType {
		ONE_MINUTE(1, ChronoUnit.MINUTES), TEN_MINUTES(10, ChronoUnit.MINUTES), HOURLY(1,
			ChronoUnit.HOURS), SIX_HOURLIES(6, ChronoUnit.HOURS), DAILY(1, ChronoUnit.DAYS);

		private final long amount;
		private final ChronoUnit chronoUnit;

		SummaryPeriodType(
			long amount,
			ChronoUnit unit
		) {
			this.amount = amount;
			this.chronoUnit = unit;
		}

		public Duration getDuration() {
			return Duration.of(amount, chronoUnit);
		}

		public static SummaryPeriodType fromString(String summaryType) {
			return switch (summaryType.toUpperCase()) {
				case "ONE_MINUTE" -> ONE_MINUTE;
				case "TEN_MINUTES" -> TEN_MINUTES;
				case "HOURLY" -> HOURLY;
				case "SIX_HOURLIES" -> SIX_HOURLIES;
				case "DAILY" -> DAILY;
				default -> throw new IllegalArgumentException("Unkown summary type: " + summaryType);
			};
		}

		// 현재 시간과 Summary periodType을 기준으로 summary할 ParameterData 마지막 시간 계산
		public LocalDateTime getCutOffTime(LocalDateTime now) {
			return switch (this) {
				case ONE_MINUTE -> now.truncatedTo(ChronoUnit.MINUTES);
				case TEN_MINUTES -> now.truncatedTo(ChronoUnit.MINUTES)
									   .withMinute((now.getMinute() / 10) * 10);
				case HOURLY -> now.truncatedTo(ChronoUnit.HOURS);
				case SIX_HOURLIES -> now.truncatedTo(ChronoUnit.HOURS)
										.withHour((now.getHour() / 6) * 6);
				case DAILY -> now.truncatedTo(ChronoUnit.DAYS);
				default -> throw new IllegalArgumentException("Unkown summary type: " + this);
			};
		}
	}

	//SummaryConfigName 기준으로 해당하는 Tool의 Summary 누락 데이터 생성을 진행
	public void initilaizeMissedSummaryProcessor(ApplicationArguments args) {
		if (!args.containsOption("name")) {
			throw new IllegalArgumentException("There is no name!");
		}
		String summaryConfigName = args.getOptionValues("name")
									   .get(0);
		List<Tool> toolList = summaryConfigToolMappingRepository.findAllToolBySummaryConfigName(summaryConfigName);

		toolList.parallelStream()
				.forEach(this::doMissedSummaryTask);
	}

	@Async
	public void doMissedSummaryTask(Tool tool) {
		boolean isMissedSummary = true;
		while (isMissedSummary) {
			//해당 툴의 파라미터 별 1분 Summary의 데이터를 SUM_END_BASE_AT가 가장 최근인것을 가져온다.
			List<SummaryData> latestSummaries = summaryDataRepository.findLatestByToolId(tool.getId());
			List<LocalDateTime> lastSummaryTimes = latestSummaries.stream()
																  .map(SummaryData::getSumEndBaseAt)
																  .toList();
			LocalDateTime now = LocalDateTime.now();
			isMissedSummary = hasTimeMissedSummaryTimes(lastSummaryTimes, now);
			if (isMissedSummary) {
				for (SummaryData summaryData : latestSummaries) {
					summarizeMissingDataByParameter(summaryData.getParameterId(), summaryData.getSumEndBaseAt(), now);
				}
			}
		}
	}

	//누락된 Summary의 Summary를 parameterId를 기준으로 마지막 Summary 시간부터 현재 시간까지 Summary를 진행
	public void summarizeMissingDataByParameter(
		Long parameterId,
		LocalDateTime startTime,
		LocalDateTime endTime
	) {
		Parameter parameter = parameterRepository.findById(parameterId)
												 .get();
		List<SummaryDataDto> summaryDataDtoList = new ArrayList<>();
		ParameterDataClassifier parameterDataClassifier = new ParameterDataClassifier();

		List<ParameterData> parameterDataList = parameterDataRepository.findByParameterIdInAndTraceAtGreaterThanEqualAndTraceAtLessThan(
			List.of(parameterId),
			startTime,
			endTime,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		parameterDataClassifier.addParameterDataList(parameterDataList);

		Map<SummaryRange, List<ParameterData>> classifiedParameterData = parameterDataClassifier.getClassifiedParameterData();
		for (Map.Entry<SummaryRange, List<ParameterData>> entry : classifiedParameterData.entrySet()) {
			com.nextorm.common.db.entity.enums.SummaryPeriodType summaryType = entry.getKey()
																					.getSummaryType();
			SummaryPeriodType summaryPeriodType = SummaryPeriodType.fromString(summaryType.name());

			//periodType에 따른 Summary해야할 마지막 시간을 계산
			LocalDateTime cutOffTime = summaryPeriodType.getCutOffTime(endTime);

			//cutOffTime 기준으로 cutOffTime 이전 데이터만 남기고 제
			List<ParameterData> filteredParameterDataList = entry.getValue()
																 .stream()
																 .filter(data -> !data.getTraceAt()
																					  .isAfter(cutOffTime))
																 .toList();

			//Summary가 필요한지 체크하고 필요한 경우 Summary를 진행
			if (isSummaryNeeded(startTime, summaryPeriodType) && !filteredParameterDataList.isEmpty()) {
				SummaryDataDto summaryDataDto = summaryProcessor.calculateSummary(entry.getKey(),
					parameter,
					filteredParameterDataList);
				summaryDataDtoList.add(summaryDataDto);
			}
		}
		summaryDataRepository.saveAll(summaryDataDtoList.stream()
														.map(SummaryDataDto::toSummaryData)
														.toList());
	}

	// lastSummaryTime과 periodType을 기준으로 Summary가 필요한지 확인
	public boolean isSummaryNeeded(
		LocalDateTime lastSummaryTime,
		SummaryPeriodType periodType
	) {
		LocalDateTime now = LocalDateTime.now();
		Duration durationSinceLastSummary = Duration.between(lastSummaryTime, now);
		return durationSinceLastSummary.compareTo(periodType.getDuration()) >= 0;
	}

	//파라미터들의 마지막 Summary시간들을 확인하여 누락된 시간이 있는 확인
	private boolean hasTimeMissedSummaryTimes(
		List<LocalDateTime> lastSummaryTimes,
		LocalDateTime now
	) {
		LocalDateTime oneMinuteAgo = now.minusMinutes(1);
		for (LocalDateTime time : lastSummaryTimes) {
			if (time.isBefore(oneMinuteAgo)) {
				return true;
			}
		}
		return false;
	}
}
