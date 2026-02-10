package com.nextorm.portal.common.exception.processoptimization;

import com.nextorm.portal.common.exception.BusinessException;

public class ProcessOptimizationNotFoundException extends BusinessException {
	public ProcessOptimizationNotFoundException() {
		super(ProcessOptimizationErrorCode.PROCESS_OPTIMIZATION_NOT_FOUND);
	}
}
