package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.SummaryConfigToolMapping;
import com.nextorm.common.db.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SummaryConfigToolMappingRepository extends JpaRepository<SummaryConfigToolMapping, Long> {
	@Query("""
				SELECT sctm.tool
					FROM SummaryConfigToolMapping  sctm
					LEFT JOIN sctm.summaryConfig
					LEFT JOIN sctm.tool
			WHERE sctm.summaryConfig.name = :summaryConfigName
		""")
	List<Tool> findAllToolBySummaryConfigName(@Param("summaryConfigName") String summaryConfigName);
}
