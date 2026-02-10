package com.nextorm.apcmodeling.config.security;

import com.nextorm.apcmodeling.config.properties.JWTProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class JWTUtil {

	public static enum TokenType {
		ACCESS_TOKEN, REFRESH_TOKEN
	}

	public static final String TOKEN_TYPE_KEY = "tokenType";
	public static final String USER_LOGIN_ID_KEY = "loginId";
	public static final String USER_ROLE_KEY = "role";

	private final JWTProperties jwtProperties;
	private final SecretKey secretKey;

	public JWTUtil(JWTProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = new SecretKeySpec(jwtProperties.getSecretKey()
														.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key()
						  .build()
						  .getAlgorithm());
	}

	public TokenType getTokenType(String token) {
		return TokenType.valueOf(Jwts.parser()
									 .verifyWith(secretKey)
									 .build()
									 .parseSignedClaims(token)
									 .getPayload()
									 .get(TOKEN_TYPE_KEY, String.class));
	}

	public String getLoginId(String token) {
		return Jwts.parser()
				   .verifyWith(secretKey)
				   .build()
				   .parseSignedClaims(token)
				   .getPayload()
				   .get(USER_LOGIN_ID_KEY, String.class);
	}

	public String getRole(String token) {
		return Jwts.parser()
				   .verifyWith(secretKey)
				   .build()
				   .parseSignedClaims(token)
				   .getPayload()
				   .get(USER_ROLE_KEY, String.class);
	}
}
