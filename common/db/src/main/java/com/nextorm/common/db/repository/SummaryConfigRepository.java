package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.SummaryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryConfigRepository extends JpaRepository<SummaryConfig, Long> {
	List<SummaryConfig> findByName(@Param("name") String name);

	Optional<SummaryConfig> findOneByName(@Param("name") String name);

	@Query("""
			SELECT distinct sc
				FROM SummaryConfig sc
				LEFT JOIN FETCH sc.toolMappings mapping
				LEFT JOIN FETCH mapping.tool
			WHERE sc.id = :id
		""")
	SummaryConfig findByIdWithTools(@Param("id") Long id);

	@Query("""
			SELECT distinct sc
				FROM SummaryConfig sc
				LEFT JOIN FETCH sc.toolMappings mapping
				LEFT JOIN FETCH mapping.tool
			WHERE sc.name = :name
		""")
	SummaryConfig findByNameWithTools(@Param("name") String name);
}
