package com.nextorm.portal.repository.processoptimization;

import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessOptimizationRepository extends JpaRepository<ProcessOptimization, Long> {

	@Query("SELECT po FROM ProcessOptimization po JOIN FETCH po.aiModel")
	List<ProcessOptimization> findAllWithAiModel(Sort sort);

	@Query("SELECT po FROM ProcessOptimization po JOIN FETCH po.aiModel WHERE po.id = :id")
	Optional<ProcessOptimization> findByIdWithAiModel(@Param("id") Long id);

	@Query("SELECT po FROM ProcessOptimization po JOIN FETCH po.aiModel am WHERE am.id = :id")
	List<ProcessOptimization> findAllWithAiModelByAiModelId(@Param("id") Long id);

	@Query("""
			DELETE
				FROM ProcessOptimization po
				WHERE po.id IN :ids
		""")
	@Modifying(clearAutomatically = true)
	void deleteAllByIdIn(@Param("ids") List<Long> ids);
}
