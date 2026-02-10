package com.nextorm.gateway.config;

import com.nextorm.gateway.exception.JwtAuthenticationError;
import com.nextorm.gateway.exception.JwtAuthenticationException;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtExceptionHandlingFilter extends AbstractGatewayFilterFactory<JwtExceptionHandlingFilter.Config> {

	public JwtExceptionHandlingFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> chain.filter(exchange)
										 .onErrorResume(e -> {
											 if (e instanceof JwtAuthenticationException) {
												 JwtAuthenticationError error = ((JwtAuthenticationException)e).getError();
												 return buildErrorResponse(exchange, error);
											 }
											 return Mono.error(e);
										 });
	}

	private Mono<Void> buildErrorResponse(
		ServerWebExchange exchange,
		JwtAuthenticationError error
	) {
		exchange.getResponse()
				.setStatusCode(error.getHttpStatusCode());
		exchange.getResponse()
				.getHeaders()
				.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		String responseStatus = String.format("{\"status\": \"%s\", \"message\": \"%s\"}",
			error.getCode(),
			error.getMessage());

		DataBuffer buffer = exchange.getResponse()
									.bufferFactory()
									.wrap(responseStatus.getBytes(StandardCharsets.UTF_8));
		return exchange.getResponse()
					   .writeWith(Mono.just(buffer));
	}

	@Data
	public static class Config {
	}
}

