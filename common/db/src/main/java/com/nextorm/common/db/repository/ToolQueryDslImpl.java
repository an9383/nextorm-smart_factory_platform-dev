package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.QTool;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.dto.ToolSearchParam;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ToolQueryDslImpl implements ToolQueryDsl {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Tool> findBySearchParams(ToolSearchParam toolSearchParam) {
		QTool tool = QTool.tool;
		return queryFactory.selectFrom(tool)
						   .where(eqLocationId(tool, toolSearchParam.getLocationId()))
						   .fetch();
	}

	private BooleanExpression eqLocationId(
		QTool tool,
		Long locationId
	) {
		if (locationId == null) {
			return null;
		}
		return tool.location.id.eq(locationId);
	}

}
