package com.nextorm.processor.config;

import com.nextorm.processor.event.redis.PortalResourceMessageListener;
import com.nextorm.processor.event.redis.ProcessorRefreshMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisSubscriberConfig {

	@Bean
	public RedisMessageListenerContainer redisContainer(
		RedisConnectionFactory connectionFactory,
		ProcessorRefreshMessageListener processorRefreshMessageListener,
		PortalResourceMessageListener portalResourceMessageListener
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		container.addMessageListener(processorRefreshMessageListener, ProcessorRefreshMessageListener.CHANNEL);
		container.addMessageListener(portalResourceMessageListener, PortalResourceMessageListener.CHANNELS);

		return container;
	}
}
