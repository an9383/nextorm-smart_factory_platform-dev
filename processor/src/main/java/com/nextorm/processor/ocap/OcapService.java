package com.nextorm.processor.ocap;

import com.nextorm.common.db.entity.OcapAlarm;
import com.nextorm.common.db.entity.OcapAlarmHistory;
import com.nextorm.common.db.entity.enums.OcapAlarmCondition;
import com.nextorm.common.db.repository.OcapAlarmHistoryRepository;
import com.nextorm.processor.event.application.FaultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcapService {
	private final OcapParameterContainer ocapParameterContainer;
	private final OcapAlarmHistoryRepository ocapAlarmHistoryRepository;

	/**
	 * OCAP 알람 대상 감지 및 처리
	 *
	 * @param message 처리할 FaultEvent 메시지
	 */
	public List<OCAP> detectAlarmTargets(FaultEvent.Message message) {
		Long parameterId = message.getParameterId();
		LocalDateTime faultAt = message.getTraceAt();

		List<OCAP> ocapList = ocapParameterContainer.getOcapList(parameterId);
		if (ocapList == null || ocapList.isEmpty()) {    // ocap 정보가 없으면 대상이 아님
			log.info("[OCAP 판단] 대상이 아니므로 스킵: {}", parameterId);
			return Collections.emptyList();
		}
		List<OCAP> alarmTargets = new ArrayList<>();

		for (OCAP ocap : ocapList) {
			if (!ocap.isAlarmControlSpecOver() && !ocap.isAlarmSpecOver()) {
				continue;
			}

			OcapAlarmCondition condition = ocap.isAlarmSpecOver()
										   ? OcapAlarmCondition.SPEC_OVER
										   : OcapAlarmCondition.CONTROL_SPEC_OVER;

			log.debug("[OCAP 판단] type: {}, parameterId: {}, name: {}",
				condition.getName(),
				ocap.getParameterId(),
				ocap.getName());
			// 각 OCAP 알람별로 마지막 전송 시간 확인
			Optional<LocalDateTime> lastSentFaultAt = ocapParameterContainer.getLastAlarmSentTime(ocap.getOcapAlarmId());

			if (lastSentFaultAt.isEmpty()) { // 처음 알람 발생
				alarmTargets.add(ocap);
				continue;
			}

			// 마지막 알람 전송 시간과 현재 알람 발생 시간 비교
			if (faultAt.isAfter(lastSentFaultAt.get()
											   .plusSeconds(ocap.getAlertIntervalSeconds()))) {
				log.info("[OCAP 판단] 알람 전송 대상: {}, 마지막 전송 시간: {}, 현재 이벤트 시간: {}",
					condition.getName(),
					lastSentFaultAt.get(),
					faultAt);
				alarmTargets.add(ocap);
			}
		}
		return alarmTargets;
	}

	@Transactional
	public void saveAlarmHistory(
		OCAP ocap,
		LocalDateTime faultAt
	) {
		log.info("saveAlarmHistory 호출 :");
		OcapAlarmCondition condition = ocap.isAlarmSpecOver()
									   ? OcapAlarmCondition.SPEC_OVER
									   : OcapAlarmCondition.CONTROL_SPEC_OVER;

		Long parameterId = ocap.getParameterId();
		Long ocapAlarmId = ocap.getOcapAlarmId();

		OcapAlarmHistory history = OcapAlarmHistory.builder()
												   .ocapAlarm(OcapAlarm.builder()
																	   .id(ocap.getOcapAlarmId())
																	   .build())
												   .parameterId(parameterId)
												   .faultAt(faultAt)
												   .alarmCondition(condition)
												   .build();

		ocapParameterContainer.updateLastAlarmSentTime(ocapAlarmId, faultAt);
		ocapAlarmHistoryRepository.save(history);
	}
}
