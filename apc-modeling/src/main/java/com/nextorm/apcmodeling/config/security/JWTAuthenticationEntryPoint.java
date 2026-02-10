package com.nextorm.apcmodeling.config.security;

import com.nextorm.apcmodeling.common.exception.JWTAuthenticationError;
import com.nextorm.apcmodeling.common.exception.JWTAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Spring Security Filter 내에서 발생되는 Exception을 ControllerAdvice에서 처리하기 위한 설정
 * (필터에서 request 객체의 attribute에 발생된 exception 객체를 추가해야 함)
 */
@Component
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final HandlerExceptionResolver resolver;

	public JWTAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) {

		Exception exception = (Exception)request.getAttribute(JWTAuthenticationException.ATTRIBUTE_KEY);
		if (exception == null) {
			exception = new JWTAuthenticationException(JWTAuthenticationError.TOKEN_INVALID);
		}

		resolver.resolveException(request, response, null, exception);
	}
}