package com.nextorm.portal.event;

import com.nextorm.portal.dto.redis.RefreshMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshMessageService {
	private final RedisPublisher redisPublisher;
	private final String PROCESSOR_REFRESH = "PROCESSOR_REFRESH_MESSAGE";
	private final String COLLECTOR_REFRESH = "COLLECTOR_REFRESH_MESSAGE";

	public void sendRefreshMessageForProcessor(
		String sender,
		Long toolId
	) {
		RefreshMessage refreshMessage = new RefreshMessage();
		refreshMessage.setTopic(PROCESSOR_REFRESH);
		refreshMessage.setSender(sender);
		refreshMessage.setToolId(String.valueOf(toolId));
		redisPublisher.publish(ChannelTopic.of(PROCESSOR_REFRESH), refreshMessage);
	}

	public void sendRefreshMessageForCollector(
		String sender,
		Long toolId
	) {
		RefreshMessage refreshMessage = new RefreshMessage();
		refreshMessage.setTopic(COLLECTOR_REFRESH);
		refreshMessage.setSender(sender);
		refreshMessage.setToolId(String.valueOf(toolId));
		redisPublisher.publish(ChannelTopic.of(COLLECTOR_REFRESH), refreshMessage);
	}
}
