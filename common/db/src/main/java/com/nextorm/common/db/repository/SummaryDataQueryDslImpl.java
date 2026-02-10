package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.QParameter;
import com.nextorm.common.db.entity.QSummaryData;
import com.nextorm.common.db.entity.QTool;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SummaryDataQueryDslImpl implements SummaryDataQueryDsl {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<SummaryData> findLatestByToolId(Long toolId) {
		QSummaryData summaryData = QSummaryData.summaryData;
		QParameter parameter = QParameter.parameter;
		QTool tool = QTool.tool;

		// 서브쿼리: 각 parameter_id와 period_type에 대해 최신 sum_end_base_at 값을 찾습니다.
		QSummaryData subSummaryData = new QSummaryData("subSummaryData");

		List<Long> latestIds = queryFactory.select(summaryData.id)
										   .from(summaryData)
										   .where(summaryData.id.in(JPAExpressions.select(subSummaryData.id)
																				  .from(subSummaryData)
																				  .innerJoin(parameter)
																				  .on(subSummaryData.parameterId.eq(
																					  parameter.id))
																				  .innerJoin(tool)
																				  .on(parameter.tool.id.eq(tool.id))
																				  .where(tool.id.eq(toolId)
																								.and(subSummaryData.sumEndBaseAt.eq(
																									JPAExpressions.select(
																													  subSummaryData.sumEndBaseAt.max())
																												  .from(
																													  subSummaryData)
																												  .where(
																													  subSummaryData.parameterId.eq(
																																		summaryData.parameterId)
																																				.and(
																																					subSummaryData.periodType.eq(
																																						summaryData.periodType)))
																												  .groupBy(
																													  subSummaryData.parameterId,
																													  subSummaryData.periodType))))))
										   .fetch();

		// 메인 쿼리 작성: latestIds 리스트에 있는 id들을 사용하여 summaryData를 조회합니다.
		return queryFactory.selectFrom(summaryData)
						   .where(summaryData.id.in(latestIds)
												.and(summaryData.periodType.eq(SummaryPeriodType.ONE_MINUTE)))
						   .fetch();
	}
}
