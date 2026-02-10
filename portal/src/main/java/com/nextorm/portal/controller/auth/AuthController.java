package com.nextorm.portal.controller.auth;

import com.nextorm.portal.config.security.JwtAuthenticationError;
import com.nextorm.portal.config.security.JwtAuthenticationException;
import com.nextorm.portal.config.security.JwtProperties;
import com.nextorm.portal.dto.auth.LoginRequestDto;
import com.nextorm.portal.dto.auth.TokenLoginRequestDto;
import com.nextorm.portal.service.auth.AuthService;
import com.nextorm.portal.service.auth.AuthToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtProperties jwtProperties;

	@PostMapping("/login")
	public ResponseEntity<Void> login(@RequestBody LoginRequestDto loginRequestDto) {
		AuthToken authToken = authService.loginProcess(loginRequestDto.getLoginId(), loginRequestDto.getPassword());
		String accessToken = jwtProperties.getAccessTokenHeaderPrefix() + authToken.getAccessToken();

		return ResponseEntity.ok()
							 .header(jwtProperties.getAccessTokenHeader(), accessToken)
							 .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(authToken.getRefreshToken()))
							 .build();
	}

	@PostMapping("/login-by-token")
	public ResponseEntity<Void> loginByToken(@RequestBody TokenLoginRequestDto loginRequestDto) {
		AuthToken authToken = authService.tokenLoginProcess(loginRequestDto.getLoginId(), loginRequestDto.getToken());
		String accessToken = jwtProperties.getAccessTokenHeaderPrefix() + authToken.getAccessToken();

		return ResponseEntity.ok()
							 .header(jwtProperties.getAccessTokenHeader(), accessToken)
							 .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(authToken.getRefreshToken()))
							 .build();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
		HttpServletRequest request
	) {
		Optional<Cookie> optionalCookie = parseRefreshTokenCookie(request.getCookies());
		if (optionalCookie.isPresent()) {
			Cookie cookie = optionalCookie.get();
			authService.deleteRefreshToken(cookie.getValue());
		}
		return ResponseEntity.ok()
							 .header(HttpHeaders.SET_COOKIE, createClearRefreshTokenCookie())
							 .build();
	}

	private Optional<Cookie> parseRefreshTokenCookie(Cookie[] cookies) {
		if (cookies == null) {
			return Optional.empty();
		}

		return Arrays.stream(cookies)
					 .filter(cookie -> jwtProperties.getRefreshTokenCookieName()
													.equals(cookie.getName()))
					 .findAny();
	}

	/**
	 * Access Token 및 Refresh Token 재발급
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/reissue-access-token")
	public ResponseEntity<Void> reissueAccessToken(
		HttpServletRequest request
	) {
		Optional<Cookie> optionalCookie = parseRefreshTokenCookie(request.getCookies());
		if (optionalCookie.isEmpty()) {
			throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
		}
		Cookie refreshTokenCookie = optionalCookie.get();
		AuthToken authToken = authService.reissueAuthenticationToken(refreshTokenCookie.getValue());
		String accessToken = jwtProperties.getAccessTokenHeaderPrefix() + authToken.getAccessToken();

		return ResponseEntity.ok()
							 .header(jwtProperties.getAccessTokenHeader(), accessToken)
							 .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(authToken.getRefreshToken()))
							 .build();
	}

	private String createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), refreshToken)
							 .maxAge(jwtProperties.getRefreshTokenExpirationSecs())
							 .httpOnly(true)
							 .path("/")
							 .build()
							 .toString();
	}

	private String createClearRefreshTokenCookie() {
		return ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), "")
							 .httpOnly(true)
							 .path("/")
							 .maxAge(0)
							 .build()
							 .toString();
	}
}
