package com.nextorm.apcmodeling.common.exception.apcmodelsimulationdata;

import com.nextorm.apcmodeling.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApcModelSimulationDataErrorCode implements BusinessErrorCode {
	APC_MODEL_SIMULATION_DATA_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_APC_MODEL_SIMULATION_DATA_NOT_FOUND",
		"시뮬레이션 데이터를 찾을 수 없습니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
