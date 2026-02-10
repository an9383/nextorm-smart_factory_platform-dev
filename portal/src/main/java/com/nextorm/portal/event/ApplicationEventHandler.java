package com.nextorm.portal.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.define.event.redis.message.OcapEventMessage;
import com.nextorm.common.define.event.redis.message.ParameterEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventHandler {
	private static final String PARAMTER_EVENT_CHANNEL = "portal:parameter";
	private static final String OCAP_EVENT_CHANNEL = "portal:ocap";

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleParameterEvent(ParameterEventMessage event) {
		redisTemplate.convertAndSend(PARAMTER_EVENT_CHANNEL, convertToJson(event));
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleOcapEvent(OcapEventMessage event) {
		redisTemplate.convertAndSend(OCAP_EVENT_CHANNEL, convertToJson(event));
	}

	private String convertToJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException("이벤트 메시지 변환중 에러 발생", e);
		}
	}

}
