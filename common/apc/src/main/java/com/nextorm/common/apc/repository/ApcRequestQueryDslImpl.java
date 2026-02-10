package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.dto.ApcRequestSearchDto;
import com.nextorm.common.apc.dto.ApcRequestStatusDto;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.entity.QApcModel;
import com.nextorm.common.apc.entity.QApcModelVersion;
import com.nextorm.common.apc.entity.QApcRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ApcRequestQueryDslImpl implements ApcRequestQueryDsl {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<ApcRequest> findBySearchParam(
		ApcRequestSearchDto apcRequestSearchDto
	) {
		QApcRequest apcRequest = QApcRequest.apcRequest;
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(apcRequest.apcModelVersionId.eq(apcRequestSearchDto.getVersionId()));
		builder.and(apcRequest.status.eq(apcRequestSearchDto.getStatus()));
		builder.and(apcRequest.createAt.between(apcRequestSearchDto.getFrom(), apcRequestSearchDto.getTo()));

		if (apcRequestSearchDto.getType() != null) {
			builder.and(apcRequest.type.eq(apcRequestSearchDto.getType()));
		}

		return queryFactory.selectFrom(apcRequest)
						   .where(builder)
						   .fetch();
	}

	@Override
	public List<ApcRequestStatusDto> findApcRequestStatusList(
		LocalDateTime from,
		LocalDateTime to,
		ApcRequest.Type type
	) {
		QApcRequest apcRequest = QApcRequest.apcRequest;
		QApcModel apcModel = QApcModel.apcModel;
		QApcModelVersion apcModelVersion = QApcModelVersion.apcModelVersion;

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(apcRequest.createAt.between(from, to));

		if (type != null) {
			builder.and(apcRequest.type.eq(type));
		}

		return queryFactory.select(Projections.fields(ApcRequestStatusDto.class,
							   apcRequest.id,
							   apcRequest.requestDataJson,
							   apcRequest.status,
							   apcRequest.createAt,
							   apcRequest.errorCode,
							   apcModel.condition.as("modelCondition"),
							   apcModel.modelName,
							   apcModelVersion.formulaWorkspace.as("formula"),
							   apcModelVersion.version))
						   .from(apcRequest)
						   .leftJoin(apcModelVersion)
						   .on(apcRequest.apcModelVersionId.eq(apcModelVersion.id))
						   .leftJoin(apcModel)
						   .on(apcModelVersion.apcModel.id.eq(apcModel.id))
						   .where(builder)
						   .orderBy(apcRequest.createAt.desc())
						   .fetch();
	}
}
