package com.nextorm.apcmodeling.common.advicehandler;

import com.nextorm.apcmodeling.common.exception.*;
import com.nextorm.apcmodeling.constant.ErrorCode;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

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
	@ExceptionHandler(JWTAuthenticationException.class)
	public ResponseEntity<JWTAuthenticationError> jwtAuthenticationException(JWTAuthenticationException e) {
		log.error("JWTAuthenticationException", e);
		JWTAuthenticationError error = e.getError();
		return new ResponseEntity<>(error, HttpStatus.valueOf(error.getHttpStatusCode()));
	}

	/**
	 * JWT 관련 예외 처리
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<JWTAuthenticationError> jwtException(JwtException e) {
		log.error("JwtException", e);
		return this.jwtAuthenticationException(new JWTAuthenticationException(JWTAuthenticationError.TOKEN_INVALID));
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<ErrorResponse> duplicationException(Exception e) {
		log.error("DuplicateKeyException", e);
		ErrorCode errorCode = ErrorCode.DUPLICATION;
		ErrorResponse errResponse = new ErrorResponse(errorCode);
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(CommonException.class)
	public ResponseEntity<ErrorResponse> commonException(CommonException e) {
		log.error("CommonException", e);
		ErrorCode errorCode = e.getErrorCode();
		ErrorResponse errResponse = new ErrorResponse(errorCode, e.getExtraData());
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> noHandlerFoundException(NoHandlerFoundException e) {
		log.error("NoHandlerFoundException", e);
		ErrorCode errorCode = ErrorCode.NOT_FOUND;
		ErrorResponse errResponse = new ErrorResponse(errorCode);
		return new ResponseEntity<>(errResponse, HttpStatus.valueOf(errorCode.getStatus()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> exception(Exception e) {
		log.error("Exception", e);
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
