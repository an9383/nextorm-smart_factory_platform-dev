package com.nextorm.common.db.repository.template;

import com.nextorm.common.db.entity.FaultHistory;

import java.util.List;

public interface FaultHistoryTemplateRepository {
	void bulkInsertFaultHistory(List<FaultHistory> faultDataList);
}
