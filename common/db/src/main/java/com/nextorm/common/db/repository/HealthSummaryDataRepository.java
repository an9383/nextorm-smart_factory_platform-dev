package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.HealthSummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthSummaryDataRepository extends JpaRepository<HealthSummaryData, Long> {
	@Query("""
				DELETE
					FROM HealthSummaryData hsd
					WHERE hsd.parameterId = :parameterId
						AND hsd.periodType IN :periodTypes
						AND hsd.trxStartAt BETWEEN :start AND :end
		""")
	@Modifying(clearAutomatically = true)
	void deleteByParameterIdAndPeriodTypeInAndTrxStartAtGreaterThanEqualAndTrxStartAtLessThan(
		@Param("parameterId") Long parameterId,
		@Param("periodTypes") List<SummaryPeriodType> periodTypes,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	List<HealthSummaryData> findAllByParameterIdInAndPeriodTypeAndTrxStartAtBetween(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("PeriodType") SummaryPeriodType periodType,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);
}
