package com.nextorm.extensions.misillan.alarm.cdc;

import com.nextorm.extensions.misillan.alarm.entity.AlarmCondition;
import com.nextorm.extensions.misillan.alarm.repository.AlarmConditionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 알람 조건 및 마지막으로 알람이 발생한 시간을 캐싱
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AlarmDataCache {
	private final AlarmConditionRepository alarmConditionRepository;

	private Map<Long, DetectCondition> conditionMap = new HashMap<>();    // KEY: 파라미터ID, VALUE: 감지조건
	private Map<Long, LocalDateTime> lastAlarmSentTime = new ConcurrentHashMap<>(); // KEY: 파라미터ID, VALUE: 마지막 알람 발생 시간

	@PostConstruct
	protected void init() {
		reCacheAll();
	}

	public void reCacheAll() {
		this.conditionMap = alarmConditionRepository.findAllWithFetch()
													.stream()
													.filter(AlarmCondition::isActive)
													.collect(Collectors.toMap(it -> it.getToolParameterMapping()
																					  .getParameter()
																					  .getId(),
														DetectCondition::of,
														(a, b) -> b,
														ConcurrentHashMap::new));

		// 더이상 존재하지 않는 파라미터ID에 대한 마지막 알람 발생 시간 정보 삭제
		for (Long parameterId : lastAlarmSentTime.keySet()) {
			if (!conditionMap.containsKey(parameterId)) {
				lastAlarmSentTime.remove(parameterId);
			}
		}
	}

	public DetectCondition findByParameterId(Long parameterId) {
		return conditionMap.get(parameterId);
	}

	public void updateDetectCondition(Long alarmConditionId) {
		Optional<AlarmCondition> found = alarmConditionRepository.findById(alarmConditionId);
		if (found.isEmpty()) {
			return;
		}

		AlarmCondition entity = found.get();
		Long parameterId = entity.getToolParameterMapping()
								 .getParameter()
								 .getId();

		if (!entity.isActive()) {
			removeByParameterId(parameterId);
			return;
		}

		conditionMap.put(parameterId, DetectCondition.of(entity));
	}

	public void removeByParameterId(Long parameterId) {
		conditionMap.remove(parameterId);
		lastAlarmSentTime.remove(parameterId);
	}

	public Optional<LocalDateTime> findLastAlarmSendTimeByParameterId(Long parameterId) {
		return Optional.ofNullable(lastAlarmSentTime.get(parameterId));
	}

	public void updateLastAlarmSendTime(
		Long parameterId,
		LocalDateTime time
	) {
		lastAlarmSentTime.put(parameterId, time);
	}
}
