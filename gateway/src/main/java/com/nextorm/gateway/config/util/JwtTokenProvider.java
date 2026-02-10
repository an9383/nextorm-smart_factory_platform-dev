package com.nextorm.gateway.config.util;

import com.nextorm.gateway.exception.JwtAuthenticationError;
import com.nextorm.gateway.exception.JwtAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = new SecretKeySpec(jwtProperties.getSecretKey()
														.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key()
						  .build()
						  .getAlgorithm());
	}

	/**
	 * 토큰 유효성 검사 메서드. 유효하지 않으면 JWTAuthenticationException 예외를 발생한다.
	 *
	 * @param token: jwt 토큰
	 */
	public void validateToken(String token) {
		try {
			parseClaims(token);
		} catch (SecurityException | MalformedJwtException | IllegalArgumentException | UnsupportedJwtException e) {
			throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID, e);
		} catch (ExpiredJwtException e) {
			throw new JwtAuthenticationException(JwtAuthenticationError.ACCESS_TOKEN_EXPIRED, e);
		}
	}

	// 토큰 파싱 및 클레임 추출
	private void parseClaims(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			throw e;
		}
	}
}
