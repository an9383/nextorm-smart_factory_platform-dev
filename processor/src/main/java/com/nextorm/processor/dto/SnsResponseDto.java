package com.nextorm.processor.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SnsResponseDto {
	private String requestId;
	private String requestTime;
	private String statusCode;
	private String statusName;
}
