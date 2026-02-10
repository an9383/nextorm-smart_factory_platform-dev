package com.nextorm.portal.common.exception.codecategory;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class CodeCategoryNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = CodeCategoryErrorCode.CODE_CATEGORY_NOT_FOUND;

	public CodeCategoryNotFoundException() {
		super(errorCode);
	}
}
