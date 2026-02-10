package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.QLocation;
import com.nextorm.common.db.entity.QReservoirCapacity;
import com.nextorm.common.db.entity.ReservoirCapacity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ReservoirCapacityQueryDslImpl implements ReservoirCapacityQueryDsl {
	private final JPAQueryFactory queryFactory;
	QReservoirCapacity reservoirCapacity = QReservoirCapacity.reservoirCapacity1;
	QLocation location = QLocation.location;

	@Override
	public List<ReservoirCapacity> findByPeriod(
		LocalDateTime from,
		LocalDateTime to
	) {
		return queryFactory.selectFrom(reservoirCapacity)
						   .leftJoin(reservoirCapacity.location, location)
						   .where(reservoirCapacity.date.between(from, to))
						   .fetch();

	}

	@Override
	public ReservoirCapacity findByReservoirCapacityId(Long id) {
		return queryFactory.selectFrom(reservoirCapacity)
						   .leftJoin(reservoirCapacity.location, location)
						   .where(reservoirCapacity.id.eq(id))
						   .fetchOne();
	}

	@Override
	public ReservoirCapacity findByLocationIdAndDate(
		Long locationId,
		LocalDateTime date
	) {
		return queryFactory.selectFrom(reservoirCapacity)
						   .leftJoin(reservoirCapacity.location, location)
						   .where(reservoirCapacity.location.id.eq(locationId)
															   .and(reservoirCapacity.date.eq(date)))
						   .fetchOne();
	}

	@Override
	public List<ReservoirCapacity> findByPeriodAndLocation(
		LocalDateTime from,
		LocalDateTime to,
		Long locationId
	) {
		return queryFactory.selectFrom(reservoirCapacity)
						   .leftJoin(reservoirCapacity.location, location)
						   .where(reservoirCapacity.date.between(from, to)
														.and(reservoirCapacity.location.id.eq(locationId)))
						   .orderBy(reservoirCapacity.date.asc())
						   .fetch();

	}

}
