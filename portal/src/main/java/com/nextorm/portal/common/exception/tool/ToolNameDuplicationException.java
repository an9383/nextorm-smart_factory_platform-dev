package com.nextorm.portal.common.exception.tool;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ToolNameDuplicationException extends BusinessException {
	private static final BusinessErrorCode errorCode = ToolErrorCode.TOOL_NAME_DUPLICATION;

	public ToolNameDuplicationException() {
		super(errorCode);
	}

	public ToolNameDuplicationException(String toolName) {
		super(errorCode);
		addExtraData("toolName", toolName);
	}
}
