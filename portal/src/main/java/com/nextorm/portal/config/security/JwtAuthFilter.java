package com.nextorm.portal.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtProperties jwtProperties;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		@NotNull HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String accessToken = parseAccessToken(request);
		try {
			if (accessToken == null) {
				throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
			}

			UserDetails userDetails = null;
			String requestUri = request.getRequestURI();

			if (isCollectorToken(accessToken, requestUri)) {
				userDetails = createUserDetails("Collector", "ADMIN");
			} else if (isAidasToken(accessToken, requestUri)) {
				userDetails = createUserDetails("AiDas", "ADMIN");
			}

			if (userDetails == null) {
				jwtTokenProvider.validateToken(accessToken);
				userDetails = parseUserDetails(accessToken);
			}

			Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails,
				null,
				userDetails.getAuthorities());

			SecurityContextHolder.getContext()
								 .setAuthentication(authToken);

		} catch (JwtAuthenticationException e) {
			request.setAttribute(JwtAuthenticationException.ATTRIBUTE_KEY, e);
		}
		filterChain.doFilter(request, response);
	}

	private String parseAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader(jwtProperties.getAccessTokenHeader());
		if (authorization == null || !authorization.startsWith(jwtProperties.getAccessTokenHeaderPrefix())) {
			return null;
		}
		return authorization.replace(jwtProperties.getAccessTokenHeaderPrefix(), "");
	}

	/**
	 * Collector API 요청인지 확인
	 *
	 * @param accessToken accessToken
	 * @param requestUri  요청 uri
	 * @return
	 */
	public boolean isCollectorToken(
		String accessToken,
		String requestUri
	) {
		if (!accessToken.equals(jwtProperties.getCollectorApiToken())) {
			return false;
		}

		return jwtProperties.getCollectorApiUri()
							.stream()
							.anyMatch(uri -> {
								Pattern pattern = Pattern.compile(this.replaceAsteriskToRegexp(uri));
								Matcher matcher = pattern.matcher(requestUri);
								return matcher.matches();
							});
	}

	/**
	 * AiDas API 요청인지 확인
	 *
	 * @param accessToken accessToken
	 * @param requestUri  요청 uri
	 * @return
	 */
	public boolean isAidasToken(
		String accessToken,
		String requestUri
	) {
		if (!accessToken.equals(jwtProperties.getAidasApiToken())) {
			return false;
		}
		return jwtProperties.getAidasApiUri()
							.stream()
							.anyMatch(uri -> {
								Pattern pattern = Pattern.compile(this.replaceAsteriskToRegexp(uri));
								Matcher matcher = pattern.matcher(requestUri);
								return matcher.matches();
							});
	}

	/**
	 * 입력된 uri 의 **, *을 정규표현식에 맞도록 변환
	 *
	 * @param uri
	 * @return
	 */
	private String replaceAsteriskToRegexp(String uri) {
		return uri.replace("**", ".+")
				  .replace("*", "[^/]+");
	}

	private UserDetails createUserDetails(
		String loginId,
		String role
	) {
		return CustomUserDetails.builder()
								.loginId(loginId)
								.role(role)
								.build();
	}

	/**
	 * Access Token 에서 사용자 정보 추출
	 *
	 * @param accessToken
	 * @return
	 */
	public UserDetails parseUserDetails(String accessToken) {
		ParsedToken parsedToken = jwtTokenProvider.parseToken(accessToken);
		if (!parsedToken.isAccessToken()) {
			throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
		}

		return CustomUserDetails.builder()
								.loginId(parsedToken.getLoginId())
								.role(parsedToken.getRole())
								.build();
	}
}
