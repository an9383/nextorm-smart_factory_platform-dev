package com.nextorm.collector.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.model.RefreshMessage;
import com.nextorm.collector.task.CollectTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscribeListener implements MessageListener {
	private final RedisTemplate<String, Object> template;
	private final CollectTaskManager collectTaskManager;
	private final ObjectMapper objectMapper;
	private final String REFRESH_MESSAGE = "COLLECTOR_REFRESH_MESSAGE";

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
				collectTaskManager.refreshCollectorByToolId(toolId);
			}

		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

}