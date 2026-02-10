package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ProcessConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessConfigRepository extends JpaRepository<ProcessConfig, Long> {
	List<ProcessConfig> findByName(@Param("name") String name);

	Optional<ProcessConfig> findOneByName(@Param("name") String name);

	@Query("""
			SELECT distinct pc
				FROM ProcessConfig pc
				LEFT JOIN FETCH pc.toolMappings mapping
				LEFT JOIN FETCH mapping.tool
			WHERE pc.id = :id
		""")
	ProcessConfig findByIdWithTools(@Param("id") Long id);

	@Query("""
			SELECT distinct pc
				FROM ProcessConfig pc
				LEFT JOIN FETCH pc.toolMappings mapping
				LEFT JOIN FETCH mapping.tool
			WHERE pc.name = :name
		""")
	ProcessConfig findByNameWithTools(@Param("name") String name);
}
