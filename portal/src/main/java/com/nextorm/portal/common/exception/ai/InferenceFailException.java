package com.nextorm.portal.common.exception.ai;

import com.nextorm.portal.common.exception.BusinessException;

public class InferenceFailException extends BusinessException {
	public InferenceFailException(String failReason) {
		super(AiModelErrorCode.INFERENCE_FAIL);
		super.addExtraData("reason", failReason);
	}
}
