package com.nextorm.gateway.config;

import com.nextorm.gateway.config.util.JwtProperties;
import com.nextorm.gateway.config.util.JwtTokenProvider;
import com.nextorm.gateway.exception.JwtAuthenticationError;
import com.nextorm.gateway.exception.JwtAuthenticationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;

	public AuthFilter(
		JwtTokenProvider jwtTokenProvider,
		JwtProperties jwtProperties
	) {
		super(Config.class);
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtProperties = jwtProperties;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return ((exchange, chain) -> {
			String accessToken = parseAccessToken(exchange.getRequest());
			try {
				if (accessToken == null) {
					throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
				}

				// 스토미용으로 발급한 토큰인지 확인하고,
				// 아니라면 일반 토큰 검증로직을 수행한다
				if (isNotStomyToken(accessToken)) {
					jwtTokenProvider.validateToken(accessToken);
				}

				return chain.filter(exchange);

			} catch (JwtAuthenticationException e) {
				throw new JwtAuthenticationException(e.getError());
			}
		});
	}

	private boolean isNotStomyToken(String token) {
		return !token.equals(jwtProperties.getStomyToken());
	}

	private String parseAccessToken(ServerHttpRequest request) {
		String authorization = request.getHeaders()
									  .getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(jwtProperties.getAccessTokenHeaderPrefix())) {
			return null;
		}
		return authorization.replace(jwtProperties.getAccessTokenHeaderPrefix(), "");
	}

	@Getter
	@Setter
	public static class Config {
	}
}
