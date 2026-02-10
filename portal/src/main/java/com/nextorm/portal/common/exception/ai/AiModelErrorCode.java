package com.nextorm.portal.common.exception.ai;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiModelErrorCode implements BusinessErrorCode {

	AI_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_AI_MODEL_NOT_FOUND",
		"AI 모델이 존재하지 않습니다."), INFERENCE_FAIL(HttpStatus.NOT_FOUND, "ERROR_INFERENCE_FAIL", "추론에 실패하였습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
