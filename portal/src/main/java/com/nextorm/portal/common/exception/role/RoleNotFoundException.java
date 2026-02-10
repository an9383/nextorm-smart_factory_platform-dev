package com.nextorm.portal.common.exception.role;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RoleNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = RoleErrorCode.ROLE_NOT_FOUND_EXCEPTION;

	public RoleNotFoundException() {
		super(errorCode);
	}

}
