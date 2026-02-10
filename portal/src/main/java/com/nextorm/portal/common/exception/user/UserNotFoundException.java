package com.nextorm.portal.common.exception.user;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = UserErrorCode.USER_NOT_FOUND;

	public UserNotFoundException() {
		super(errorCode);
	}

}
