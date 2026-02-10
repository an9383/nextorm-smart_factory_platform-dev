package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Tool;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long>, ToolQueryDsl {
	@Query("""
			SELECT tool
				FROM Tool tool
			WHERE tool.id IN (
				SELECT pctm.tool.id
				FROM ProcessConfigToolMapping pctm
					LEFT JOIN pctm.processConfig
				WHERE pctm.processConfig.name = :processConfigName
			)
		""")
	List<Tool> findAllByProcessConfigName(@Param("processConfigName") String processConfigName);

	List<Tool> findByLocationId(
		@Param("id") Long id,
		Sort createAt
	);

	@Query("""
			SELECT t 
				FROM Tool t 
				LEFT JOIN FETCH t.location l 
				LEFT JOIN FETCH l.parent lp
				LEFT JOIN FETCH lp.parent lgp 
				ORDER BY :#{#sort}
		""")
	List<Tool> findAllWithLocation(Sort createAt);

	List<Tool> findByLocationIdIn(
		@Param("locationIds") List<Long> locationIds,
		Sort sort
	);

	Optional<Tool> findByLocationIdAndName(
		@Param("locationId") Long locationId,
		@Param("name") String name
	);

	@Query("""
			SELECT t 
				FROM Tool t 
				LEFT JOIN FETCH t.location l 
				LEFT JOIN FETCH l.parent lp
				LEFT JOIN FETCH lp.parent lgp 
				 WHERE t.id IN :ids
		""")
	List<Tool> findAllWithLocationById(
		@Param("ids") Set<Long> ids
	);
}
