package com.nextorm.common.db.repository;

import com.nextorm.common.db.repository.dto.ParameterRawDataStatistics;

import java.time.LocalDateTime;
import java.util.List;

public interface ParameterDataQueryDsl {
	List<ParameterRawDataStatistics> getParameterDataStatistics(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	);
}
