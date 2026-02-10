package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.CollectorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectorConfigRepository extends JpaRepository<CollectorConfig, Long> {
	CollectorConfig findByName(@Param("name") String name);

	Optional<CollectorConfig> findOneByName(@Param("name") String name);

	@Query("""
			SELECT distinct cc
				FROM CollectorConfig cc
				LEFT JOIN FETCH cc.toolMappings mapping
				LEFT JOIN FETCH mapping.tool
			WHERE cc.id = :id
		""")
	CollectorConfig findByIdWithTools(@Param("id") Long id);
}
