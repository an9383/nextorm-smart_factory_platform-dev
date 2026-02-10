package com.nextorm.portal.common.exception.ai;

import com.nextorm.portal.common.exception.BusinessException;

public class AiModelNotFoundException extends BusinessException {
	public AiModelNotFoundException() {
		super(AiModelErrorCode.AI_MODEL_NOT_FOUND);
	}
}
