package com.nextorm.processor.ocap;

import com.nextorm.common.db.entity.OcapAlarm;
import com.nextorm.common.db.entity.OcapAlarmRecipient;
import com.nextorm.common.db.repository.OcapAlarmHistoryRepository;
import com.nextorm.common.db.repository.OcapAlarmRecipientRepository;
import com.nextorm.common.db.repository.OcapAlarmRepository;
import com.nextorm.processor.ocap.user.User;
import com.nextorm.processor.ocap.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcapParameterContainer {

	// key: parameterId
	private final Map<Long, List<OCAP>> parameterOcapMap = new HashMap<>();
	// key를 ocapAlarmId로 사용(parameterId -> ocapAlarmId로) , value: 마지막으로 알람을 보낸 시간
	private final Map<Long, LocalDateTime> lastAlarmSentMap = new HashMap<>();

	private final OcapAlarmRepository ocapAlarmRepository;
	private final OcapAlarmRecipientRepository ocapAlarmRecipientRepository;
	private final OcapAlarmHistoryRepository ocapAlarmHistoryRepository;
	private final UserRepository userRepository;

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * 클래스 초기화 시 OCAP 알람 정보를 로드하고 매핑하는 메소드
	 * DB에서 모든 OCAP 알람 정보를 조회하여 메모리에 캐싱하고,
	 * 최근 알람 발생 시간도 함께 로드한다
	 */
	@Transactional(readOnly = true)
	@PostConstruct
	public void init() {
		LocalDateTime currentTime = LocalDateTime.now();

		List<OcapAlarm> ocapAlarms = ocapAlarmRepository.findAllWithFetch();

		Map<Long, List<OcapAlarm>> ocapAlarmMapByParameter = ocapAlarms.stream()
																	   .collect(Collectors.groupingBy(alarm -> alarm.getParameter()
																													.getId()));

		for (Map.Entry<Long, List<OcapAlarm>> entry : ocapAlarmMapByParameter.entrySet()) {
			Long parameterId = entry.getKey();
			List<OCAP> ocapList = new ArrayList<>();

			for (OcapAlarm ocapAlarm : entry.getValue()) {
				OCAP ocap = createOcap(ocapAlarm);
				ocapList.add(ocap);

				LocalDateTime alarmSearchCutoffTime = currentTime.minusSeconds(ocap.getAlertIntervalSeconds() * 2L);
				ocapAlarmHistoryRepository.findByOcapAlarmIdAndFaultAt(ocapAlarm.getId(), alarmSearchCutoffTime)
										  .ifPresent(alarmHistory -> lastAlarmSentMap.put(ocap.getParameterId(),
											  alarmHistory.getFaultAt()));
			}
			parameterOcapMap.put(parameterId, ocapList);
		}
	}

	private OCAP createOcap(OcapAlarm ocapAlarm) {
		List<OcapAlarmRecipient> alarmRecipients = ocapAlarmRecipientRepository.findByOcapAlarmId(ocapAlarm.getId());
		List<User> users = userRepository.findByIdIn(alarmRecipients.stream()
																	.map(OcapAlarmRecipient::getUserId)
																	.toList());
		return OCAP.from(ocapAlarm, alarmRecipients, users);
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * OCAP 알람 ID로 알람 정보를 업데이트하는 메소드
	 * 해당 ID의 알람 정보를 다시 조회하여 캐시를 갱신한다.
	 *
	 * @param ocapAlarmId 업데이트할 OCAP 알람 ID
	 */
	@Transactional(readOnly = true)
	public void upsertByOcapId(Long ocapAlarmId) {
		ocapAlarmRepository.findByIdWithFetch(ocapAlarmId)
						   .ifPresent(ocapAlarm -> {
							   OCAP ocap = createOcap(ocapAlarm);
							   Long parameterId = ocap.getParameterId();

							   List<OCAP> ocapList = parameterOcapMap.computeIfAbsent(parameterId,
								   key -> new ArrayList<>());

							   ocapList.removeIf(existing -> existing.getOcapAlarmId()
																	 .equals(ocapAlarmId));
							   ocapList.add(ocap);
						   });
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * 파라미터 ID로 알람 정보를 업데이트하는 메소드
	 * 캐시에 해당 파라미터 ID가 있는 경우에만 갱신 작업을 수행한다.
	 *
	 * @param parameterId 업데이트할 파라미터 ID
	 */
	@Transactional(readOnly = true)
	public void updateByParameterId(Long parameterId) {
		if (!parameterOcapMap.containsKey(parameterId)) {
			return;
		}

		List<OCAP> ocapList = new ArrayList<>();
		List<OcapAlarm> allByParameterIdWithFetch = ocapAlarmRepository.findAllByParameterIdWithFetch(parameterId);

		for(OcapAlarm ocapAlarm : allByParameterIdWithFetch) {
			OCAP ocap = createOcap(ocapAlarm);
			ocapList.add(ocap);
		}
		parameterOcapMap.put(parameterId, ocapList);
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * OCAP 알람 ID로 캐시된 알람 정보를 제거하는 메소드
	 * 해당 알람 ID에 연결된 파라미터 정보와 최근 알람 발송 시간도 함께 제거한다.
	 *
	 * @param ocapAlarmId 제거할 OCAP 알람 ID
	 */
	@Transactional(readOnly = true)
	public void removeByOcapId(Long ocapAlarmId) {
		for(Map.Entry<Long, List<OCAP>> entry : parameterOcapMap.entrySet()) {
			Long parameterId = entry.getKey();
			List<OCAP> ocapList = entry.getValue();
			Optional<OCAP> targetOcap = ocapList.stream()
										   .filter(ocap -> ocap.getOcapAlarmId()
															   .equals(ocapAlarmId))
										   .findFirst();
			if(targetOcap.isPresent()) {
				ocapList.remove(targetOcap.get());
				if(ocapList.isEmpty()) {
					parameterOcapMap.remove(parameterId);
					lastAlarmSentMap.remove(parameterId);
				}
				break;
			}
		}
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * 파라미터 ID에 해당하는 OCAP 알람 정보를 조회하는 메소드
	 *
	 * @param parameterId 조회할 파라미터 ID
	 * @return 해당 파라미터의 OCAP 알람 정보
	 */
	public List<OCAP> getOcapList(Long parameterId) {
		return parameterOcapMap.getOrDefault(parameterId, Collections.emptyList());
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * 파라미터 ID에 대한 마지막 알람 발송 시간을 조회하는 메소드
	 *
	 * @param ocapAlarmId 조회할 파라미터 ID
	 * @return 마지막 알람 발송 시간 (Optional)
	 */
	public Optional<LocalDateTime> getLastAlarmSentTime(Long ocapAlarmId) {
		return Optional.ofNullable(lastAlarmSentMap.get(ocapAlarmId));
	}

	/**
	 * 코파일럿으로 작성한 주석입니다<p>
	 * 파라미터 ID에 대한 마지막 알람 발송 시간을 업데이트하는 메소드
	 *
	 * @param ocapAlarmId       업데이트할 파라미터 ID
	 * @param lastAlarmSentTime 마지막 알람 발송 시간
	 */
	public void updateLastAlarmSentTime(
		Long ocapAlarmId,
		LocalDateTime lastAlarmSentTime
	) {
		lastAlarmSentMap.put(ocapAlarmId, lastAlarmSentTime);
	}
}
