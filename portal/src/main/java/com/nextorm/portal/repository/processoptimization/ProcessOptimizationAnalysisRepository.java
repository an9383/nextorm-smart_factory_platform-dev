package com.nextorm.portal.repository.processoptimization;

import com.nextorm.portal.entity.processoptimization.ProcessOptimizationAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessOptimizationAnalysisRepository extends JpaRepository<ProcessOptimizationAnalysis, Long> {

	Optional<ProcessOptimizationAnalysis> findByProcessOptimizationId(Long process_optimization_id);

	@Query("""
			DELETE
				FROM ProcessOptimizationAnalysis poa
				WHERE poa.processOptimization.id IN :ids
		""")
	@Modifying(clearAutomatically = true)
	void deleteAllByProcessOptimizationIdIn(@Param("ids") List<Long> ids);
}
