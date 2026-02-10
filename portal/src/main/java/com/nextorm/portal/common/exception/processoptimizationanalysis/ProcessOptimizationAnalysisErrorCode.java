package com.nextorm.portal.common.exception.processoptimizationanalysis;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ProcessOptimizationAnalysisErrorCode implements BusinessErrorCode {

	PROCESS_OPTIMIZATION_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_PROCESS_OPTIMIZATION_ANALYSIS_NOT_FOUND",
		"공정 최적화 결과가 존재하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
