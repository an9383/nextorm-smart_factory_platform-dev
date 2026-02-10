package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ProcessConfigToolMapping;
import com.nextorm.common.db.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessConfigToolMappingRepository extends JpaRepository<ProcessConfigToolMapping, Long> {
	@Query("""
				SELECT pctm.tool
					FROM ProcessConfigToolMapping  pctm
					LEFT JOIN pctm.processConfig
					LEFT JOIN pctm.tool
			WHERE pctm.processConfig.name = :processConfigName
		""")
	List<Tool> findAllToolByProcessConfigName(@Param("processConfigName") String processConfigName);
}
