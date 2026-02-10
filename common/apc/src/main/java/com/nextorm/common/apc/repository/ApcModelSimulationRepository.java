package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcModelSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApcModelSimulationRepository extends JpaRepository<ApcModelSimulation, Long> {

	@Query("""
		SELECT s 
		  FROM ApcModelSimulation s
		  LEFT JOIN FETCH s.apcModelVersion v
		  LEFT JOIN FETCH v.apcModel m 
		  LEFT JOIN FETCH s.apcModelSimulationDataList d
		 WHERE s.id = :id
		""")
	Optional<ApcModelSimulation> findByIdWithSimulationData(@Param("id") Long id);

	@Query("""
		SELECT s 
		  FROM ApcModelSimulation s
		  LEFT JOIN FETCH s.apcModelVersion v
		  LEFT JOIN FETCH v.apcModel m 
		  LEFT JOIN FETCH s.apcModelSimulationDataList d
		 WHERE s.createAt BETWEEN :from AND :to
		 ORDER BY s.createAt DESC
		""")
	List<ApcModelSimulation> findWithSimulationDataByCreateAtBetweenOrderByCreateAtDesc(
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);
}