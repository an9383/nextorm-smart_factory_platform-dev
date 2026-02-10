package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.OcapAlarmHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OcapAlarmHistoryRepository extends JpaRepository<OcapAlarmHistory, Long> {
	@Query("""
			SELECT oah
			  FROM OcapAlarmHistory oah
			  LEFT JOIN FETCH oah.ocapAlarm oa
			  LEFT JOIN FETCH oa.tool t
			  LEFT JOIN FETCH oa.parameter p
			where oah.faultAt between :from and :to
		  ORDER BY oah.id DESC
		""")
	List<OcapAlarmHistory> findAllByFaultAtBetweenOrderByIdDescWithDeepFetch(
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	@Query("""
			SELECT oah
			  FROM OcapAlarmHistory oah
			 WHERE oah.ocapAlarm.id = :ocapAlarmId
			   AND oah.faultAt >= :faultAt
		  ORDER BY oah.id DESC
		  LIMIT 1
		""")
	Optional<OcapAlarmHistory> findByOcapAlarmIdAndFaultAt(
		@Param("ocapAlarmId") Long ocapAlarmId,
		@Param("faultAt") LocalDateTime faultAt
	);

	@Modifying
	@Query("""
		DELETE FROM OcapAlarmHistory oah
		 WHERE oah.ocapAlarm.id = :ocapAlarmId
		""")
	void deleteByOcapAlarmId(@Param("ocapAlarmId") Long ocapAlarmId);
}