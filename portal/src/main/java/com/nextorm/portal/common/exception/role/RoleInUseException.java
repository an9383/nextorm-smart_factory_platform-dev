package com.nextorm.portal.common.exception.role;

import com.nextorm.portal.common.exception.BusinessException;

public class RoleInUseException extends BusinessException {
	public RoleInUseException() {
		super(RoleErrorCode.ROLE_IN_USE);
	}
}
