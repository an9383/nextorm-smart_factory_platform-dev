package com.nextorm.apcmodeling.config.security;

import com.nextorm.apcmodeling.common.exception.JWTAuthenticationError;
import com.nextorm.apcmodeling.common.exception.JWTAuthenticationException;
import com.nextorm.apcmodeling.config.properties.JWTProperties;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final JWTProperties jwtProperties;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader(jwtProperties.getAccessTokenHeader());
		if (authorization == null || !authorization.startsWith(jwtProperties.getAccessTokenHeaderPrefix())) {
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = authorization.replace(jwtProperties.getAccessTokenHeaderPrefix(), "");

		try {
			UserDetails userDetails = getUserDetailsByAccessToken(accessToken);
			Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
				null,
				userDetails.getAuthorities());
			SecurityContextHolder.getContext()
								 .setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			Exception exception = e;
			if (exception instanceof ExpiredJwtException) {
				//ControllerAdvice에서 예외를 처리하기 위해 Exception 변환
				exception = new JWTAuthenticationException(JWTAuthenticationError.ACCESS_TOKEN_EXPIRED);
			}
			request.setAttribute(JWTAuthenticationException.ATTRIBUTE_KEY, exception);
			throw e;
		}
	}

	private UserDetails getUserDetailsByAccessToken(String accessToken) {
		//토큰 타입이 Access 토큰인지 확인
		if (jwtUtil.getTokenType(accessToken) != JWTUtil.TokenType.ACCESS_TOKEN) {
			throw new JWTAuthenticationException(JWTAuthenticationError.TOKEN_INVALID);
		}

		String loginId = jwtUtil.getLoginId(accessToken);
		String role = jwtUtil.getRole(accessToken);

		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

		return new UserDetails() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return List.of(authority);
			}

			@Override
			public String getPassword() {
				return "";
			}

			@Override
			public String getUsername() {
				return loginId;
			}

			@Override
			public boolean isAccountNonExpired() {
				return true;
			}

			@Override
			public boolean isAccountNonLocked() {
				return true;
			}

			@Override
			public boolean isCredentialsNonExpired() {
				return true;
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};
	}
}
