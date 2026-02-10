package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.repository.dto.FaultCount;
import com.nextorm.common.db.repository.template.FaultHistoryTemplateRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaultHistoryRepository extends JpaRepository<FaultHistory, Long>, FaultHistoryTemplateRepository {
	List<FaultHistory> findByParameterIdAndFaultAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	List<FaultHistory> findByParameterIdAndFaultAtBetweenOrderByFaultAtAsc(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	@Query("""
			DELETE
				FROM FaultHistory fh
				WHERE fh.parameterId = :parameterId
					AND fh.faultAt BETWEEN :from AND :to
		""")
	@Modifying(clearAutomatically = true)
	int deleteByParameterIdAndFaultAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	@Query("""
			SELECT fh.parameterId AS parameterId, COUNT(fh.parameterId) AS count
				FROM FaultHistory fh
				WHERE fh.parameterId IN :parameterIds
					AND fh.faultAt BETWEEN :from AND :to
				GROUP BY fh.parameterId
		""")
	List<FaultCount> groupParameterIdByParameterIdAndFaultAtBetween(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);
}
