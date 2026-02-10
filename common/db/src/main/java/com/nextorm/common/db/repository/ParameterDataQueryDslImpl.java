package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.QParameter;
import com.nextorm.common.db.entity.QParameterData;
import com.nextorm.common.db.repository.dto.ParameterRawDataStatistics;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ParameterDataQueryDslImpl implements ParameterDataQueryDsl {
	private final JPAQueryFactory queryFactory;

	QParameterData parameterData = QParameterData.parameterData;
	QParameter parameter = QParameter.parameter;

	@Override
	public List<ParameterRawDataStatistics> getParameterDataStatistics(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {

		return queryFactory.select(Projections.constructor(ParameterRawDataStatistics.class,
							   parameter.id,
							   parameter.name,
							   numberDataValueField(parameter.dataType).avg(),
							   numberDataValueField(parameter.dataType).min(),
							   numberDataValueField(parameter.dataType).max()))
						   .from(parameterData)
						   .join(parameter)
						   .on(parameterData.parameterId.eq(parameter.id))
						   .where(parameter.id.in(parameterIds)
											  .and(parameter.dataType.in(Parameter.DataType.INTEGER,
												  Parameter.DataType.DOUBLE)))
						   .groupBy(parameterData.parameterId)
						   .fetch();
	}

	private NumberExpression<Double> numberDataValueField(EnumPath<Parameter.DataType> dataType) {
		return new CaseBuilder().when(dataType.eq(Parameter.DataType.DOUBLE))
								.then(parameterData.dValue)
								.when(dataType.eq(Parameter.DataType.INTEGER))
								.then(parameterData.iValue.doubleValue())
								.otherwise(0.0);
	}

}
