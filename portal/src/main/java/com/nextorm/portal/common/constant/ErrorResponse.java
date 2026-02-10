package com.nextorm.portal.common.constant;

import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private String code;

    private Map<String, Object> extraData;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.message = errorCode.getMessage();
        this.code = errorCode.getErrorCode();
    }

    public ErrorResponse(ErrorCode errorCode, Map<String, Object> extraData) {
        this.status = errorCode.getStatus();
        this.message = errorCode.getMessage();
        this.code = errorCode.getErrorCode();
        this.extraData = extraData;
    }
}