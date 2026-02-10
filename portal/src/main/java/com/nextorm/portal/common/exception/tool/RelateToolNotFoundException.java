package com.nextorm.portal.common.exception.tool;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RelateToolNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ToolErrorCode.TOOL_NOT_FOUND;

	public RelateToolNotFoundException() {
		super(errorCode);
	}

	public RelateToolNotFoundException(Long toolId) {
		super(errorCode);
		addExtraData("toolId", toolId);
	}
}
