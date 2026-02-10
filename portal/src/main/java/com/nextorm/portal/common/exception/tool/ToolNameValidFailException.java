package com.nextorm.portal.common.exception.tool;

import com.nextorm.portal.common.exception.BusinessException;

public class ToolNameValidFailException extends BusinessException {
	public ToolNameValidFailException() {
		super(ToolErrorCode.TOOL_NAME_VALID_FAIL);
	}
}
