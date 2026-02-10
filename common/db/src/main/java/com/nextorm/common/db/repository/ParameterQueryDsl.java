package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.repository.dto.ParameterSearchParam;

import java.util.List;

public interface ParameterQueryDsl {
	List<Parameter> findBySearchParams(ParameterSearchParam searchParam);
}
