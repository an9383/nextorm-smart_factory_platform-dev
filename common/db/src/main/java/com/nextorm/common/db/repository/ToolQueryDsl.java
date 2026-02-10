package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.dto.ToolSearchParam;

import java.util.List;

public interface ToolQueryDsl {
	List<Tool> findBySearchParams(ToolSearchParam toolSearchParam);
}
