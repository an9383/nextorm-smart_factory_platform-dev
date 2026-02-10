package com.nextorm.processor.event.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.define.event.redis.message.OcapEventMessage;
import com.nextorm.common.define.event.redis.message.ParameterEventMessage;
import com.nextorm.processor.ocap.OcapParameterContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortalResourceMessageListener implements MessageListener {
	private static final String PORTAL_CHANNEL_PREFIX = "portal:";

	private static final String PARAMETER_CHANNEL = PORTAL_CHANNEL_PREFIX + "parameter";
	private static final String OCAP_CHANNEL = PORTAL_CHANNEL_PREFIX + "ocap";

	public static final List<ChannelTopic> CHANNELS = List.of(new ChannelTopic(PARAMETER_CHANNEL),
		new ChannelTopic(OCAP_CHANNEL));

	private final ObjectMapper objectMapper;
	private final OcapParameterContainer ocapParameterContainer;

	enum ResourceType {
		PARAMETER, OCAP;

		public static ResourceType from(String redisChannel) {
			return switch (redisChannel) {
				case PARAMETER_CHANNEL -> PARAMETER;
				case OCAP_CHANNEL -> OCAP;
				default -> null;
			};
		}
	}

	@Override
	public void onMessage(
		Message message,
		byte[] pattern
	) {
		String channel = new String(message.getChannel());

		ResourceType resourceType = ResourceType.from(channel);
		if (resourceType == null) {
			log.warn("관리하지 않는 리소스 채널: {}", channel);
			return;
		}

		if (ResourceType.PARAMETER == resourceType) {
			ParameterEventMessage eventMessage = convertMessageToObject(message.getBody(), ParameterEventMessage.class);
			log.debug("parameterEventMessage: {}", eventMessage);
			ocapParameterContainer.updateByParameterId(eventMessage.getParameterId());
			return;
		}

		if (ResourceType.OCAP == resourceType) {
			OcapEventMessage eventMessage = convertMessageToObject(message.getBody(), OcapEventMessage.class);
			log.debug("ocapEventMessage: {}", eventMessage);
			handleOcapEvent(eventMessage);
			return;
		}

		log.info("핸들링되지 않은 타입: {}", resourceType);
	}

	public <T> T convertMessageToObject(
		byte[] message,
		Class<T> targetType
	) {
		try {
			return objectMapper.readValue(message, targetType);
		} catch (Exception e) {
			throw new RuntimeException("포탈 이벤트 역직렬화중 에러 발생", e);
		}
	}

	private void handleOcapEvent(OcapEventMessage eventMessage) {
		OcapEventMessage.Type type = eventMessage.getType();
		switch (type) {
			case CREATE, MODIFY -> ocapParameterContainer.upsertByOcapId(eventMessage.getOcapAlarmId());
			case DELETE -> ocapParameterContainer.removeByOcapId(eventMessage.getOcapAlarmId());
			default -> log.warn("알 수 없는 OCAP 이벤트 유형: {}", type);
		}
	}
}
