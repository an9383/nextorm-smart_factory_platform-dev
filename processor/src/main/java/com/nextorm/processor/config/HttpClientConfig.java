package com.nextorm.processor.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {
	@Bean
	public HttpClient httpClient() {

		int maxPool = 200; // 전체 커넥션 풀에서 사용할 수 있는 최대 커넥션 수
		int maxPerRoute = 100; // 각 호스트(서버)당 커넥션 풀에서 사용할 수 있는 최대 커넥션 수
		long validateAfterInactivity = 5L; // 유휴상태 검증 시간
		int idleConnectionTimeoutSec = 30; // 유휴 커넥션이 이 시간이 지나면 풀에서 제거되는 시간
		long requestTimeOut = 5000L; // 커넥션 풀에서 커넥션을 얻기 위해 대기할 수 있는 최대 시간
		long readTimeOut = 3000L; // 서버로부터 응답을 기다리는 최대 시간
		int retryCount = 3; // 예외가 발생했을 때 요청을 재시도할 최대 횟수
		long backoff = 2000; // 재시도 간의 대기 시간

		// 커넥션 관리 매니저 (커넥션 풀을 관리하는 매니저)
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(maxPool);
		connManager.setDefaultMaxPerRoute(maxPerRoute);
		connManager.setDefaultConnectionConfig(ConnectionConfig.custom()
															   .setValidateAfterInactivity(Timeout.ofSeconds(
																   validateAfterInactivity))
															   .build()); // 유휴 상태에서 커넥션을 재사용하기 전에 유효성 검사를 수행 (서버와의 연결이 끊겼는지 확인)

		// 요청 관련 다양한 설정을 제공 (타임아웃 관련 설정)
		RequestConfig requestConfig = RequestConfig.custom()
												   .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeOut))
												   .setResponseTimeout(Timeout.ofMilliseconds(readTimeOut))
												   .build();

		return HttpClients.custom()
						  .setConnectionManager(connManager)
						  .setDefaultRequestConfig(requestConfig)
						  .setRetryStrategy(new DefaultHttpRequestRetryStrategy(retryCount,
							  TimeValue.ofMilliseconds(backoff))) // 네트워크 오류 또는 일시적인 서버 문제 발생 시 재시도 전략
						  .evictIdleConnections(TimeValue.ofSeconds(idleConnectionTimeoutSec)) // 유휴 커넥션이 사용되지 않았을 경우 커넥션을 종료하고 풀에서 제거
						  .build();
	}

	@Bean
	public RestTemplate restTemplate(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		// 콘텐츠 길이 자동계산 및 헤더에 자동 설정
		BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);
		return new RestTemplate(bufferingFactory);
	}

}
