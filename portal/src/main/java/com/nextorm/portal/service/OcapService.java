package com.nextorm.portal.service;

import com.nextorm.common.db.entity.OcapAlarm;
import com.nextorm.common.db.entity.OcapAlarmRecipient;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.*;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.common.define.event.redis.message.OcapEventMessage;
import com.nextorm.portal.common.exception.code.CodeNotFoundException;
import com.nextorm.portal.common.exception.parameter.ParameterNotFoundException;
import com.nextorm.portal.common.exception.tool.ToolNotFoundException;
import com.nextorm.portal.dto.ocap.*;
import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.repository.system.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OcapService {

	private final OcapAlarmRepository ocapAlarmRepository;
	private final OcapAlarmRecipientRepository ocapAlarmRecipientRepository;
	private final OcapAlarmHistoryRepository ocapAlarmHistoryRepository;

	private final CodeRepository codeRepository;
	private final ToolRepository toolRepository;
	private final UserRepository userRepository;
	private final ParameterRepository parameterRepository;

	private final ApplicationEventPublisher eventPublisher;

	public List<OcapResponseDto> getAll() {
		return ocapAlarmRepository.findAllWithFetch()
								  .stream()
								  .map(this::enrichAndMapToOcapResponseDto)
								  .toList();
	}

	private OcapResponseDto enrichAndMapToOcapResponseDto(OcapAlarm ocapAlarm) {
		List<OcapAlarmRecipient> alarmRecipients = ocapAlarmRecipientRepository.findByOcapAlarmId(ocapAlarm.getId());
		List<User> users = userRepository.findByIdIn(alarmRecipients.stream()
																	.map(OcapAlarmRecipient::getUserId)
																	.toList());
		return OcapResponseDto.from(ocapAlarm, alarmRecipients, users);
	}

	public OcapResponseDto getById(Long id) {
		OcapAlarm ocapAlarm = ocapAlarmRepository.findByIdWithFetch(id)
												 .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 OCAP ID입니다."));
		return this.enrichAndMapToOcapResponseDto(ocapAlarm);
	}

	public void create(OcapCreateRequestDto requestDto) {
		Code code = codeRepository.findById(requestDto.getAlarmIntervalCodeId())
								  .orElseThrow(CodeNotFoundException::new);

		Tool tool = toolRepository.findById(requestDto.getToolId())
								  .orElseThrow(ToolNotFoundException::new);

		Parameter parameter = parameterRepository.findById(requestDto.getParameterId())
												 .orElseThrow(ParameterNotFoundException::new);

		OcapAlarm ocapAlarm = OcapAlarm.create(requestDto.getName(),
			requestDto.isAlarmControlSpecOver(),
			requestDto.isAlarmSpecOver(),
			code,
			tool,
			parameter);

		List<OcapAlarmRecipient> ocapAlarmRecipients = createOcapAlarmRecipients(ocapAlarm,requestDto.getRecipients());

		ocapAlarmRepository.save(ocapAlarm);
		ocapAlarmRecipientRepository.saveAll(ocapAlarmRecipients);

		eventPublisher.publishEvent(OcapEventMessage.createEvent(ocapAlarm.getId()));
	}

	private List<OcapAlarmRecipient> createOcapAlarmRecipients(
		OcapAlarm ocapAlarm,
		List<RecipientInfo> recipients
	) {
		List<OcapAlarmRecipient> ocapAlarmRecipientList = new ArrayList<>();
		for(RecipientInfo recipientInfo : recipients) {
			Long userId = recipientInfo.getUserId();
			for(String notificationType : recipientInfo.getNotificationTypes()) {
				OcapAlarmRecipient recipient = OcapAlarmRecipient.create(
					ocapAlarm,
					userId,
					notificationType
				);
				ocapAlarmRecipientList.add(recipient);
			}
		}
		return  ocapAlarmRecipientList;
	}

	public void modify(
		Long ocapAlarmId,
		OcapModifyRequestDto requestDto
	) {

		OcapAlarm ocapAlarm = ocapAlarmRepository.findById(ocapAlarmId)
												 .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 OCAP ID입니다."));

		Code code = codeRepository.findById(requestDto.getAlarmIntervalCodeId())
								  .orElseThrow(CodeNotFoundException::new);

		Tool tool = toolRepository.findById(requestDto.getToolId())
								  .orElseThrow(ToolNotFoundException::new);

		Parameter parameter = parameterRepository.findById(requestDto.getParameterId())
												 .orElseThrow(ParameterNotFoundException::new);

		List<OcapAlarmRecipient> ocapAlarmRecipients = createOcapAlarmRecipients(ocapAlarm,requestDto.getRecipients());

		// 삭제 후 재생성
		ocapAlarmRecipientRepository.deleteByOcapAlarmId(ocapAlarmId);

		ocapAlarm.modify(requestDto.getName(),
			requestDto.isAlarmControlSpecOver(),
			requestDto.isAlarmSpecOver(),
			code,
			tool,
			parameter);
		ocapAlarmRecipientRepository.saveAll(ocapAlarmRecipients);

		eventPublisher.publishEvent(OcapEventMessage.modifyEvent(ocapAlarm.getId()));
	}

	public List<OcapAlarmHistoryResponseDto> getHistories(OcapAlarmHistorySearchRequestDto searchParam) {
		return ocapAlarmHistoryRepository.findAllByFaultAtBetweenOrderByIdDescWithDeepFetch(searchParam.getFromDate(),
											 searchParam.getToDate())
										 .stream()
										 .map(OcapAlarmHistoryResponseDto::of)
										 .toList();
	}

	public void delete(Long ocapAlarmId) {
		ocapAlarmHistoryRepository.deleteByOcapAlarmId(ocapAlarmId);
		ocapAlarmRecipientRepository.deleteByOcapAlarmId(ocapAlarmId);
		ocapAlarmRepository.deleteById(ocapAlarmId);

		eventPublisher.publishEvent(OcapEventMessage.deleteEvent(ocapAlarmId));
	}
}
