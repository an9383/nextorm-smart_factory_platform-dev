package com.nextorm.extensions.misillan.alarm.exception.toolparametermapping;

import com.nextorm.extensions.misillan.alarm.exception.BusinessException;

public class ToolParameterMappingNotFoundException extends BusinessException {
	public ToolParameterMappingNotFoundException(){
		super(ToolParameterMappingErrorCode.TOOL_PARAMETER_MAPPING_NOT_FOUND);
	}
}
