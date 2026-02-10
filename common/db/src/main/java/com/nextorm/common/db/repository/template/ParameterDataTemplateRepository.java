package com.nextorm.common.db.repository.template;

import com.nextorm.common.db.entity.ParameterData;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParameterDataTemplateRepository {
	List<ParameterData> findLatestParameterData(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("baseDateTime") LocalDateTime baseDateTime
	);

	void bulkInsertParameterData(List<ParameterData> parameterDataList);

	void bulkInsertParameterDataRequireValue(List<ParameterData> parameterDataList);
}
