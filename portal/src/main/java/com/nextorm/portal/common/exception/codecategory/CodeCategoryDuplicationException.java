package com.nextorm.portal.common.exception.codecategory;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class CodeCategoryDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = CodeCategoryErrorCode.CODE_CATEGORY_DUPLICATION;

	public CodeCategoryDuplicationException(String codeCategory) {
		super(errorCode);
		addExtraData("codeCategory", codeCategory);
	}
}
