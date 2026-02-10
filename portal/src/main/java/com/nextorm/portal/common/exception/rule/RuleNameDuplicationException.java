package com.nextorm.portal.common.exception.rule;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RuleNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = RuleErrorCode.RULE_NAME_DUPLICATION;

	public RuleNameDuplicationException(String ruleName) {
		super(errorCode);
		addExtraData("ruleName", ruleName);
	}

}
