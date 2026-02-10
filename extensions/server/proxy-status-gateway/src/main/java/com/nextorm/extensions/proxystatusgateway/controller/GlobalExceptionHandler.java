package com.nextorm.extensions.proxystatusgateway.controller;

import com.nextorm.extensions.proxystatusgateway.dto.ErrorResponse;
import com.nextorm.extensions.proxystatusgateway.exception.ServerNotFoundException;
import com.nextorm.extensions.proxystatusgateway.exception.StatusServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 위한 컨트롤러 어드바이스
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * 서버를 찾을 수 없는 경우 (404 Not Found)
	 */
	@ExceptionHandler(ServerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleServerNotFoundException(ServerNotFoundException e) {
		log.warn("서버를 찾을 수 없음: {}", e.getServerName());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
							 .body(ErrorResponse.notFound(e.getServerName()));
	}

	/**
	 * 서비스 에러 (500 Internal Server Error)
	 */
	@ExceptionHandler(StatusServiceException.class)
	public ResponseEntity<ErrorResponse> handleStatusServiceException(StatusServiceException e) {
		log.error("서비스 에러 발생", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							 .body(ErrorResponse.internalError(e.getMessage()));
	}

	/**
	 * 기타 예상치 못한 에러 (500 Internal Server Error)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("예기치 못한 오류 발생", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							 .body(ErrorResponse.internalError("예기치 못한 오류가 발생했습니다"));
	}
}
