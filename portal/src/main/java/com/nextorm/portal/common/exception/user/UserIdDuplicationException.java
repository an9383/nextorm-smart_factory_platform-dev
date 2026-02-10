package com.nextorm.portal.common.exception.user;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class UserIdDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = UserErrorCode.USER_ID_DUPLICATION;

	public UserIdDuplicationException(String userId) {
		super(errorCode);
		addExtraData("userId", userId);
	}
}
