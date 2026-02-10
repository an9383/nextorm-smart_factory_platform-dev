package com.nextorm.collector.common.handler;

import com.nextorm.collector.common.BusinessErrorCode;
import com.nextorm.collector.common.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerAdviceHandler {

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
