package com.nextorm.portal.common.exception.role;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RoleNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = RoleErrorCode.ROLE_NAME_DUPLICATION;

	public RoleNameDuplicationException(String roleName) {
		super(errorCode);
		addExtraData("roleName", roleName);
	}
}
