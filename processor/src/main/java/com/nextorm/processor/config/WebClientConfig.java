package com.nextorm.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	private static final int MAX_RESPONSE_BODY_SIZE = 2 * 1024 * 1024; // 2MB

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
													  .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(MAX_RESPONSE_BODY_SIZE)) // SMS는 작은 데이터
													  .build())
                .build();
    }
}
