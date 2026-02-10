package com.nextorm.portal.common.handler;

import com.nextorm.portal.common.constant.ErrorCode;
import com.nextorm.portal.common.constant.ErrorResponse;
import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;
import com.nextorm.portal.common.exception.CommonException;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.config.security.JwtAuthenticationError;
import com.nextorm.portal.config.security.JwtAuthenticationException;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdviceHandler {
	/**
	 * JWT 인증 관련 예외 처리
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(JwtAuthenticationException.class)
	public ResponseEntity<JwtAuthenticationError> jwtAuthenticationException(JwtAuthenticationException e) {
		JwtAuthenticationError error = e.getError();
		log.debug("{}", error.name(), e);
		return new ResponseEntity<>(error, HttpStatus.valueOf(error.getHttpStatusCode()));
	}

	/**
	 * JWT 관련 예외 처리
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<JwtAuthenticationError> jwtException(JwtException e) {
		log.debug("{}", e.getMessage(), e);
		return this.jwtAuthenticationException(new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID));
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<ErrorResponse> duplicationException(Exception e) {
		ErrorCode errorCode = ErrorCode.DUPLICATION;
		log.debug("{}: {}", errorCode.getErrorCode(), e.getMessage(), e);

		ErrorResponse errResponse = new ErrorResponse(errorCode);
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(ConstraintViloationException.class)
	public ResponseEntity<ErrorResponse> constraintViloationException(ConstraintViloationException e) {
		ErrorCode errorCode = ErrorCode.CONSTRAINT_VIOLATION;
		log.debug("{}: {}", errorCode.getErrorCode(), e.getMessage(), e);

		Map<String, Object> extraData = new HashMap<>();
		extraData.put("datas", e.getData());
		ErrorResponse errResponse = new ErrorResponse(errorCode, extraData);
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(CommonException.class)
	public ResponseEntity<ErrorResponse> commonException(CommonException e) {
		log.debug("{}",
			e.getErrorCode()
			 .getErrorCode(),
			e);
		ErrorCode errorCode = e.getErrorCode();
		ErrorResponse errResponse = new ErrorResponse(errorCode, e.getExtraData());
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> exception(Exception e) {
		log.debug("{}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		ErrorResponse errResponse = new ErrorResponse(errorCode);
		return new ResponseEntity<>(errResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<BusErrorResponse> businessException(BusinessException e) {
		log.debug("code: {}, extraData: {}", e.getErrorCode(), e.getExtraData(), e);

		BusinessErrorCode errorCode = e.getErrorCode();
		BusErrorResponse errResponse = new BusErrorResponse(errorCode.getCode(),
			errorCode.getMessage(),
			e.getExtraData());

		return new ResponseEntity<>(errResponse,
			e.getErrorCode()
			 .getHttpStatus());
	}

	@Getter
	@AllArgsConstructor
	public static class BusErrorResponse {
		private String code;
		private String message;
		private Map<String, Object> extraData = Map.of();
	}
}
