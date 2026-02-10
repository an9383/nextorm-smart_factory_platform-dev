package com.nextorm.portal.common.exception.tool;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ToolNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ToolErrorCode.TOOL_NOT_FOUND;

	public ToolNotFoundException() {
		super(errorCode);
	}

}
