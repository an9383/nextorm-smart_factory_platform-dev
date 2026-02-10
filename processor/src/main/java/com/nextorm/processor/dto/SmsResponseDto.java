package com.nextorm.processor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsResponseDto {
	private String requestId;
	private String requestTime;
	private String statusCode;
	private String statusName;
}
