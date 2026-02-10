package com.nextorm.apcmodeling.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorCode {
	NOT_FOUND(404, "ERROR_NOT_FOUND", "Page Not Found"),

	UNPROCESSABLE_ENTITY(422, "ERROR_UNPROCESSABLE_ENTITY", "Data Unprocessable"),

	INTERNAL_SERVER_ERROR(500, "ERROR_COMMON", "Internal Server Error"),

	DUPLICATION(409, "ERROR_DUPLICATION", "Data duplicated"),

	RELATED_DATA_EXISTS(409, "ERROR_RELATED_DATA_EXISTS", "Related data exists"),

	DUPLICATION_LOCATION_NAME_TYPE_PARENT(409, "ERROR_DUPLICATION_TYPE_NAME", "Data duplicated from Type in Name"),

	DUPLICATION_PARAMETER_NAME(409, "ERROR_DUPLICATION_PARAMETER_NAME", "Data duplicated from ParameterName"),

	DUPLICATION_PROCESS_CONFIG_NAME(409,
		"ERROR_DUPLICATION_PROCESS_CONFIG_NAME",
		"Data duplicated from ProecessConfigName"),

	DUPLICATION_COLLECTOR_CONFIG_NAME(409,
		"ERROR_DUPLICATION_COLLECTOR_CONFIG_NAME",
		"Data duplicated from CollectorConfigName"),

	DUPLICATION_RULE_NAME(409, "ERROR_DUPLICATION_RULE_NAME", "Data duplicated from RuleName"),

	DUPLICATION_TOOL_NAME_LOCATION(409,
		"ERROR_DUPLICATION_TOOL_NAME_LOCATION",
		"Data duplicated from ToolName in Location"),

	DUPLICATION_KEY(409, "ERROR_DUPLICATION_KEY", "Data duplicated key"),

	CONSTRAINT_VIOLATION(409, "ERROR_CONSTRAINT_VIOLATION", "Data Constraint Violation"),

	CODE_RECURSIVE(422, "ERROR_CODE_RECURSIVE", "Save Code is recursive.");

	private int status;
	private String errorCode;
	private String message;

	public int getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}
}