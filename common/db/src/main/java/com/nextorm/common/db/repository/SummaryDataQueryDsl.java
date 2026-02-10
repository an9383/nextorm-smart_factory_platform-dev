package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.SummaryData;

import java.util.List;

public interface SummaryDataQueryDsl {
	List<SummaryData> findLatestByToolId(Long toolId);
}
