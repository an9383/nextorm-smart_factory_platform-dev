package com.nextorm.extensions.misillan.alarm.cdc;

import com.nextorm.extensions.misillan.alarm.entity.AlarmHistory;
import com.nextorm.extensions.misillan.alarm.entity.ConditionType;
import com.nextorm.extensions.misillan.alarm.repository.AlarmHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AlarmDetectService {

	private final AlarmDataCache alarmDataCache;
	private final AlarmHistoryRepository alarmHistoryRepository;
	private final long alarmIntervalSeconds; // 알람 재발생 주기

	public AlarmDetectService(
		AlarmDataCache alarmDataCache,
		AlarmHistoryRepository alarmHistoryRepository,
		@Value("${alarm.interval-seconds:60}") long alarmIntervalSeconds
	) {
		this.alarmDataCache = alarmDataCache;
		this.alarmHistoryRepository = alarmHistoryRepository;
		this.alarmIntervalSeconds = alarmIntervalSeconds;
	}

	public void processAlarmDetection(ParameterData parameterData) {
		Long parameterId = parameterData.getParameterId();
		DetectCondition detectCondition = this.alarmDataCache.findByParameterId(parameterId);
		if (detectCondition == null) {
			return;
		}

		boolean conditionMatched = switch (detectCondition.conditionType()) {
			case PRESSURE -> parameterData.getValue() >= detectCondition.pressure();
			case TEMPERATURE -> parameterData.getValue() >= detectCondition.temperature();
			case METAL_DETECT -> parameterData.getValue() > 0; // 금속검출은 0보다 크면 알람
		};

		boolean isTimerOver = false;
		if (detectCondition.activeAt() != null && detectCondition.timerSeconds() != null) {
			LocalDateTime timerEndAt = detectCondition.activeAt()
													  .plusSeconds(detectCondition.timerSeconds());
			long diffSeconds = Duration.between(timerEndAt, LocalDateTime.now())
									   .toSeconds();
			if (diffSeconds >= 0 && diffSeconds <= 4) {
				isTimerOver = true;
			} else {
				isTimerOver = false;
			}
		}

		// 알람 조건이 맞거나, 타이머가 종료된 경우
		boolean isAlarmTarget = conditionMatched || isTimerOver;

		if (isAlarmTarget && !isRecentAlarm(parameterId)) {
			LocalDateTime now = LocalDateTime.now();
			alarmDataCache.updateLastAlarmSendTime(parameterId, now);
			log.info("알람 발생! - parameterData: {}, condition: {}", parameterData, detectCondition);

			/**
			 * 알람 발생 이력 저장
			 */
			AlarmHistory alarmHistory = new AlarmHistory();
			alarmHistory.setParameterId(parameterData.getParameterId());
			alarmHistory.setValue(parameterData.getValue());
			alarmHistory.setAlarmDt(now);
			alarmHistory.setConditionType(toAlarmHistoryType(detectCondition.conditionType()));
			alarmHistory.setToolName(detectCondition.toolName());
			alarmHistory.setToolId(detectCondition.toolId());
			alarmHistory.setProductId(detectCondition.productId());
			alarmHistory.setProductName(detectCondition.productName());
			alarmHistory.setParameterName(detectCondition.parameterName());
			switch (detectCondition.conditionType()) {
				case PRESSURE -> alarmHistory.setThreshold(detectCondition.pressure());     // 압력 기준값
				case TEMPERATURE -> alarmHistory.setThreshold(detectCondition.temperature()); // 온도 기준값
				case METAL_DETECT -> alarmHistory.setThreshold(0.0);                    // 금속 검출 기준값
			}
			if (isTimerOver) {
				alarmHistory.setTriggerType(AlarmHistory.TriggerType.TIME_OVER);
			} else if (conditionMatched) {
				alarmHistory.setTriggerType(AlarmHistory.TriggerType.VALUE_OVER);
			}

			alarmHistoryRepository.save(alarmHistory);
		}
	}

	public static ConditionType toAlarmHistoryType(ConditionType type) {
		return switch (type) {
			case PRESSURE -> ConditionType.PRESSURE;
			case TEMPERATURE -> ConditionType.TEMPERATURE;
			case METAL_DETECT -> ConditionType.METAL_DETECT;
		};
	}

	/**
	 * 최근 알람 발생 여부 확인
	 *
	 * @param parameterId
	 * @return true: 최근 알람 발생이 ALARM_INTERVAL_SECONDS 이내
	 * <br/>false: 알람 발생 이력이 없거나, 최근 알람 발생이 ALARM_INTERVAL_SECONDS 이상 지남
	 */
	private boolean isRecentAlarm(Long parameterId) {
		return alarmDataCache.findLastAlarmSendTimeByParameterId(parameterId)
							 .map(lastAlarmSentTime -> {
								 // 현재 시간이 최근 알람 발생 시간 + ALARM_INTERVAL_SECONDS 보다 이전이라면 '최근에 발생한 알람'
								 LocalDateTime now = LocalDateTime.now();
								 return now.isBefore(lastAlarmSentTime.plusSeconds(alarmIntervalSeconds));
							 })
							 .orElse(false);
	}
}
