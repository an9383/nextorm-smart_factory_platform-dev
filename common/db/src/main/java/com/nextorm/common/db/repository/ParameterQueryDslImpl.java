package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.QParameter;
import com.nextorm.common.db.repository.dto.ParameterSearchParam;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ParameterQueryDslImpl implements ParameterQueryDsl {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Parameter> findBySearchParams(ParameterSearchParam searchParam) {
		QParameter parameter = QParameter.parameter;
		return queryFactory.selectFrom(parameter)
						   .where(idIn(parameter, searchParam.getIds()),
							   toolIdEq(parameter, searchParam.getToolId()),
							   eqName(parameter, searchParam.getName()),
							   isVirtualEq(parameter, searchParam.getIsVirtual()),
							   eqType(parameter, searchParam.getType()),
							   dataTypeIn(parameter, searchParam.getDataTypes()))
						   .orderBy(parameter.name.asc())
						   .fetch();
	}

	private BooleanExpression isVirtualEq(
		QParameter parameter,
		Boolean isVirtual
	) {
		if (isVirtual == null) {
			return null;
		}
		return isVirtual
			   ? parameter.isVirtual.isTrue()
			   : parameter.isVirtual.isFalse();
	}

	private BooleanExpression toolIdEq(
		QParameter parameter,
		Long toolId
	) {
		if (toolId == null) {
			return null;
		}
		return parameter.tool.id.eq(toolId);
	}

	private BooleanExpression eqName(
		QParameter parameter,
		String name
	) {
		if (name == null) {
			return null;
		}
		return parameter.name.eq(name);
	}

	private BooleanExpression idIn(
		QParameter parameter,
		List<Long> ids
	) {
		if (ids == null || ids.isEmpty()) {
			return null;
		}
		return parameter.id.in(ids);
	}

	private BooleanExpression eqType(
		QParameter parameter,
		Parameter.Type type
	) {
		if (type == null) {
			return null;
		}
		return parameter.type.eq(type);
	}

	private BooleanExpression dataTypeIn(
		QParameter parameter,
		List<Parameter.DataType> dataTypes
	) {
		if (dataTypes == null || dataTypes.isEmpty()) {
			return null;
		}
		return parameter.dataType.in(dataTypes);
	}
}
