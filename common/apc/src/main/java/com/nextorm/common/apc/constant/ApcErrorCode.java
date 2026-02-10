package com.nextorm.common.apc.constant;

public enum ApcErrorCode {
	INVALID_REQUEST_DATA, //요청 데이터가 없거나 JSON 형식에 안맞는 경우
	INVALID_CONDITION, //요청 데이터 중 Condition이 시스템에 정의된 Condition과 맞지 않는 경우
	FORMULA_MISMATCH,  //요청 데이터가 정의된 모델 Formula와 맞지 않는 경우
	FORMULA_EXECUTION_ERROR, //Formula 실행 중 에러 발생 시 
	EXTERNAL_SERVER_COMMUNICATION_ERROR, //외부 서버와 통신 중 에러 발생 시
	INTERNAL_SERVER_ERROR //Runtime 에러 발생 시
}
