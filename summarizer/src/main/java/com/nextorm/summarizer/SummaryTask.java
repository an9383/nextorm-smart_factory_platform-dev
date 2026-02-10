package com.nextorm.summarizer;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.SummaryConfigToolMappingRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.summarizer.service.summaryprocessor.SummaryProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync
public class SummaryTask {
	private boolean isEnabled = false;
	private final SummaryConfigToolMappingRepository summaryConfigToolMappingRepository;
	private final ToolRepository toolRepository;
	private final ParameterRepository parameterRepository;
	private final SummaryProcessor summaryProcessor;

	private final Map<Long, com.nextorm.summarizer.SummaryTimeTracker> timeTrackerMap = new ConcurrentHashMap<>();
	private List<Tool> toolList = new ArrayList<>();

	public void initializeSummaryTask(ApplicationArguments args) {
		if (!args.containsOption("name")) {
			throw new IllegalArgumentException("There is no name!");
		}
		String summaryConfigName = args.getOptionValues("name")
									   .get(0);
		toolList = summaryConfigToolMappingRepository.findAllToolBySummaryConfigName(summaryConfigName);
		toolList.forEach(tool -> timeTrackerMap.put(tool.getId(), new SummaryTimeTracker()));
	}

	@Scheduled(cron = "05 0/1 * * * ?")
	public void scheduleTasks() {
		if (isEnabled) {
			toolList.parallelStream()
					.forEach(this::doSummaryTask);
		}
	}

	@Async
	public void doSummaryTask(Tool tool) {
		SummaryTimeTracker timeTracker = timeTrackerMap.get(tool.getId());
		long startTime = System.nanoTime();

		try {
			List<Parameter> targetParameters = getParametersByToolId(tool);
			LocalDateTime now = LocalDateTime.now();

			if (targetParameters.isEmpty()) {
				return;
			}

			// 분 단위
			if (timeTracker.getCheckMinute() != now.getMinute()) {
				SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(now,
					SummaryPeriodType.ONE_MINUTE);
				timeTracker.updateTrackerBySummaryType(now, summaryRange.getSummaryType());

				loggingSummaryStart(tool.getName(), summaryRange.getSummaryType());
				summaryProcessor.process(summaryRange, targetParameters);
			}

			// 분 단위 외에는 섬머리 대상 여부를 체크하여 처리
			Optional<SummaryPeriodType> targetSummaryType = checkSummaryTarget(timeTracker, now);
			targetSummaryType.ifPresent(summaryType -> {
				log.info("[{}] targetSummaryType : {}", tool.getName(), targetSummaryType);

				timeTracker.updateTrackerBySummaryType(now, summaryType);
				SummaryRange summaryRange = SummaryRange.createNearestPastSummaryRange(now, targetSummaryType.get());

				loggingSummaryStart(tool.getName(), summaryRange.getSummaryType());
				summaryProcessor.process(summaryRange, targetParameters);
			});

			double timeDiff = (System.nanoTime() - startTime) / 1000000.0;
			if (timeDiff > 500) {
				log.info("{} : " + "The Processing Time of SummaryTask for Summary Type : {} is over than 500 ms : {} [ms]",
					tool.getName(),
					targetSummaryType,
					timeDiff);
			}
			log.info("{} : SummaryTask Exit", tool.getName());
		} catch (RuntimeException e) {
			log.warn("", e);
		}
	}

	private Optional<SummaryPeriodType> checkSummaryTarget(
		SummaryTimeTracker timeTracker,
		LocalDateTime baseTime
	) {
		if (timeTracker.getCheck10Minutes() != (baseTime.getMinute() - (baseTime.getMinute() % 10))) {
			return Optional.of(SummaryPeriodType.TEN_MINUTES);
		}
		if (timeTracker.getCheckHour() != baseTime.getHour()) {
			return Optional.of(SummaryPeriodType.HOURLY);
		}
		if (timeTracker.getCheckSixHour() != (baseTime.getHour() - (baseTime.getHour() % 6))) {
			return Optional.of(SummaryPeriodType.SIX_HOURLIES);
		}
		if (timeTracker.getCheckDay() != baseTime.getDayOfMonth()) {
			return Optional.of(SummaryPeriodType.DAILY);
		}
		return Optional.empty();
	}

	private List<Parameter> getParametersByToolId(Tool tool) {
		return parameterRepository.findByToolId(tool.getId(), Sort.by(Sort.Direction.ASC, "name"));
	}

	private void loggingSummaryStart(
		String toolName,
		SummaryPeriodType summaryType
	) {
		log.info("doSummary Start - Tool : {}, Summary Type : {}", toolName, summaryType.name());
	}

	public void enableScheduler() {
		this.isEnabled = true;
	}
}

