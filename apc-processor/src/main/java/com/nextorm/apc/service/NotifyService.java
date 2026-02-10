package com.nextorm.apc.service;

import com.nextorm.apc.alertchannel.NotifyChannel;
import com.nextorm.apc.alertchannel.NotifyMessage;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.entity.ApcRequestResult;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import com.nextorm.common.apc.repository.ApcRequestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifyService {
	private final ApcRequestRepository apcRequestRepository;
	private final ApcRequestResultRepository apcRequestResultRepository;
	private final ApcModelVersionRepository apcModelVersionRepository;
	private final List<NotifyChannel> notifyChannels;

	@Async
	@Transactional(readOnly = true)
	public void asyncNotify(Long apcRequestId) {
		log.info("call notify service. apcRequestId: {}", apcRequestId);
		ApcRequest apcRequest = apcRequestRepository.findById(apcRequestId)
													.orElseThrow(() -> new IllegalArgumentException(
														"Not found apcRequestId: " + apcRequestId));

		if (!validateNotifyTarget(apcRequest)) {
			return;
		}

		Map<String, Object> resultsMap = apcRequestResultRepository.findByApcRequestId(apcRequestId)
																   .stream()
																   .collect(Collectors.toMap(ApcRequestResult::getResultKey,
																	   ApcRequestResult::getResultValue));

		NotifyMessage notifyMessage = new NotifyMessage(apcRequestId,
			apcRequest.getStatus()
					  .name(),
			apcRequest.getCreateAt(),
			apcRequest.getRequestDataJson(),
			resultsMap);

		for (NotifyChannel notifyChannel : notifyChannels) {
			notifyChannel.notify(notifyMessage);
		}
	}

	private boolean validateNotifyTarget(ApcRequest apcRequest) {
		Long apcRequestId = apcRequest.getId();
		if (!apcRequest.isSuccess()) {
			log.info("APC가 성공하지 않으므로 알림을 보내지 않음. apcRequestId: {}", apcRequestId);
			return false;
		}

		ApcModelVersion apcModelVersion = apcModelVersionRepository.findByApcModelVersionId(apcRequest.getApcModelVersionId());
		if (apcModelVersion == null) {
			log.info("알림여부를 확인할 ApcModelVersion을 찾지 못함. apcRequestId: {}", apcRequestId);
			return false;
		}
		if (!apcModelVersion.isUseNotify()) {
			log.info("알림을 사용하지 않는 ApcModelVersion.id: {}, apcRequestId: {}", apcModelVersion.getId(), apcRequestId);
			return false;
		}
		return true;
	}
}
