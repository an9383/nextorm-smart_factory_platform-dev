package com.nextorm.portal.common.exception.rule;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RuleNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = RuleErrorCode.RULE_NOT_FOUND_EXCEPTION;

	public RuleNotFoundException() {
		super(errorCode);
	}

}
