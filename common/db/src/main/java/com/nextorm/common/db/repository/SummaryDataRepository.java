package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SummaryDataRepository extends JpaRepository<SummaryData, Long>, SummaryDataQueryDsl {
	List<SummaryData> findAllByParameterIdAndPeriodTypeAndTrxStartAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("PeriodType") SummaryPeriodType periodType,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	// sd.sumStartBaseAt >= :fromDate AND :toDate > sd.sumStartBaseAt
	List<SummaryData> findByParameterIdInAndSumStartBaseAtGreaterThanEqualAndSumStartBaseAtLessThanAndPeriodType(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("fromDate") LocalDateTime fromDate,
		@Param("toDate") LocalDateTime toDate,
		@Param("periodType") SummaryPeriodType periodType
	);

	List<SummaryData> findAllByParameterIdAndPeriodTypeAndTrxStartAtBetweenOrderByTrxStartAtAsc(
		@Param("parameterId") Long parameterId,
		@Param("PeriodType") SummaryPeriodType periodType,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);

	@Query("""
				DELETE
					FROM SummaryData sd
					WHERE sd.parameterId = :parameterId
						AND sd.periodType IN :periodTypes
						AND sd.trxStartAt BETWEEN :start AND :end
		""")
	@Modifying(clearAutomatically = true)
	void deleteByParameterIdAndPeriodTypeInAndTrxStartAtGreaterThanEqualAndTrxStartAtLessThan(
		@Param("parameterId") Long parameterId,
		@Param("periodTypes") List<SummaryPeriodType> periodTypes,
		@Param("start") LocalDateTime start,
		@Param("end") LocalDateTime end
	);
}
