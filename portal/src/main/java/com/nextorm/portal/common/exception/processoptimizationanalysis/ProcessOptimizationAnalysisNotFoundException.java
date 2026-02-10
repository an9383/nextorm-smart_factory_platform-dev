package com.nextorm.portal.common.exception.processoptimizationanalysis;

import com.nextorm.portal.common.exception.BusinessException;

public class ProcessOptimizationAnalysisNotFoundException extends BusinessException {
	public ProcessOptimizationAnalysisNotFoundException() {
		super(ProcessOptimizationAnalysisErrorCode.PROCESS_OPTIMIZATION_ANALYSIS_NOT_FOUND);
	}
}
