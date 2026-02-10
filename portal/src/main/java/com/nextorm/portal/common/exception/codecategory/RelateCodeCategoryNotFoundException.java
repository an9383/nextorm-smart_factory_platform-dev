package com.nextorm.portal.common.exception.codecategory;

import com.nextorm.portal.common.exception.BusinessException;

public class RelateCodeCategoryNotFoundException extends BusinessException {
	public RelateCodeCategoryNotFoundException() {
		super(CodeCategoryErrorCode.RELATE_CODE_CATEGORY_NOT_FOUND);
	}
}
