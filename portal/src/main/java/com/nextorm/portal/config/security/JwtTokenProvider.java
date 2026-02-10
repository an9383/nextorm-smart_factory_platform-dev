package com.nextorm.portal.config.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
	public static final String TOKEN_TYPE_KEY = "tokenType";
	public static final String USER_LOGIN_ID_KEY = "loginId";
	public static final String USER_ROLE_KEY = "role";

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

	public ParsedToken parseToken(String token) {
		Claims claims = parseClaims(token);
		TokenType tokenType = TokenType.valueOf(parseClaims(token).get(TOKEN_TYPE_KEY, String.class));

		return ParsedToken.builder()
						  .loginId(claims.get(USER_LOGIN_ID_KEY, String.class))
						  .role(claims.get(USER_ROLE_KEY, String.class))
						  .isAccessToken(tokenType == TokenType.ACCESS_TOKEN)
						  .expiration(claims.getExpiration())
						  .issuedAt(claims.getIssuedAt())
						  .build();
	}

	public String generateAccessToken(
		String loginId,
		String role
	) {
		return Jwts.builder()
				   .claim(TOKEN_TYPE_KEY, TokenType.ACCESS_TOKEN)
				   .claim(USER_LOGIN_ID_KEY, loginId)
				   .claim(USER_ROLE_KEY, role)
				   .issuedAt(new Date(System.currentTimeMillis()))
				   .expiration(new Date(System.currentTimeMillis() + (jwtProperties.getAccessTokenExpirationSecs() * 1000L)))
				   .signWith(secretKey)
				   .compact();

	}

	public String generateRefreshToken(
		String loginId,
		String role
	) {
		return Jwts.builder()
				   .claim(TOKEN_TYPE_KEY, TokenType.REFRESH_TOKEN)
				   .claim(USER_LOGIN_ID_KEY, loginId)
				   .claim(USER_ROLE_KEY, role)
				   .issuedAt(new Date(System.currentTimeMillis()))
				   .expiration(new Date(System.currentTimeMillis() + (jwtProperties.getRefreshTokenExpirationSecs() * 1000L)))
				   .signWith(secretKey)
				   .compact();

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
	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					   .verifyWith(secretKey)
					   .build()
					   .parseSignedClaims(token)
					   .getPayload();
		} catch (ExpiredJwtException e) {
			throw e;
		}
	}

	enum TokenType {
		ACCESS_TOKEN, REFRESH_TOKEN
	}
}
