package com.nextorm.extensions.proxystatusgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 에러 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	/**
	 * HTTP 상태 코드
	 */
	private int status;

	/**
	 * 에러 발생 이유
	 */
	private String reason;

	/**
	 * 404 Not Found 에러 응답 생성
	 */
	public static ErrorResponse notFound(String serverName) {
		return new ErrorResponse(404, "서버 '" + serverName + "'를 찾을 수 없습니다");
	}

	/**
	 * 500 Internal Server Error 응답 생성
	 */
	public static ErrorResponse internalError(String message) {
		return new ErrorResponse(500, message);
	}
}
