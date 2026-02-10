package com.nextorm.collector.config;

import com.nextorm.collector.listener.RedisSubscribeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		return template;
	}

	@Bean
	public ChannelTopic topic() {
		return new ChannelTopic("messageQueue");
	}

	public ChannelTopic collectorRefreshTopic() {
		return new ChannelTopic("COLLECTOR_REFRESH_MESSAGE");
	}

	@Bean
	public RedisMessageListenerContainer redisContainer(
		@Qualifier("refreshMessageListener") MessageListenerAdapter listenerAdapter,
		@Qualifier("messageListener") MessageListenerAdapter messageListener
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory());
		container.addMessageListener(messageListener(), topic());
		container.addMessageListener(listenerAdapter, collectorRefreshTopic());
		return container;
	}

	@Bean
	public MessageListenerAdapter refreshMessageListener(RedisSubscribeListener subscriber) {
		return new MessageListenerAdapter(subscriber);
	}

	@Bean
	public MessageListenerAdapter messageListener() {
		return new MessageListenerAdapter(new RedisSubscriber());
	}

	@Slf4j
	static class RedisSubscriber {
		public void handleMessage(String message) {
			log.info("Received <{}>", message);
		}
	}
}
