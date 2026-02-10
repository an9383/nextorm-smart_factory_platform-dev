package com.nextorm.processor.event.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.processor.ProcessorHandler;
import com.nextorm.processor.model.RefreshMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorRefreshMessageListener implements MessageListener {
	private static final String REFRESH_MESSAGE = "PROCESSOR_REFRESH_MESSAGE";

	public static final ChannelTopic CHANNEL = new ChannelTopic(REFRESH_MESSAGE);

	private final RedisTemplate<String, Object> template;
	private final ProcessorHandler processorHandler;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(
		Message message,
		byte[] pattern
	) {
		try {
			String publishMessage = template.getStringSerializer()
											.deserialize(message.getBody());

			RefreshMessage refreshMessage = objectMapper.readValue(publishMessage, RefreshMessage.class);

			log.info("Redis Subscribe Channel : " + refreshMessage.getTopic());
			log.info("Redis SUB Message : {}", publishMessage);

			if (REFRESH_MESSAGE.equals(refreshMessage.getTopic())) {
				Long toolId = Long.parseLong(refreshMessage.getToolId());
				processorHandler.refreshTool(toolId);
			}

		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

}
