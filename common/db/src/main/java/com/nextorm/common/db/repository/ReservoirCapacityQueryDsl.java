package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ReservoirCapacity;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservoirCapacityQueryDsl {
	List<ReservoirCapacity> findByPeriod(
		LocalDateTime from,
		LocalDateTime to
	);

	ReservoirCapacity findByReservoirCapacityId(Long id);

	ReservoirCapacity findByLocationIdAndDate(
		Long locationId,
		LocalDateTime date
	);

	List<ReservoirCapacity> findByPeriodAndLocation(
		LocalDateTime from,
		LocalDateTime to,
		Long locationId
	);

}
