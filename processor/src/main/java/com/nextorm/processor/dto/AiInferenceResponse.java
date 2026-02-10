package com.nextorm.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiInferenceResponse {
	private boolean success;
	private String message;
	private Double data;
}
