package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.repository.template.LocationTemplateRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, LocationTemplateRepository {
	List<Location> findByParent(
		Location parent,
		Sort sort
	);

	@Query("SELECT l FROM Location l LEFT JOIN FETCH l.parent gl LEFT JOIN FETCH gl.parent ORDER BY :#{#sort}")
	List<Location> findAllWithParent(Sort sort);

	Optional<Location> findByNameAndTypeAndParent(
		@Param("name") String name,
		@Param("type") Location.Type type,
		@Param("parent") Location parent
	);

	@Query("""
		SELECT l
			FROM Location l
			WHERE l.name = :name
			AND l.type = :type
			AND l.parent.id = :parentId
		""")
	Optional<Location> findByNameAndTypeAndParentId(
		@Param("name") String name,
		@Param("type") Location.Type type,
		@Param("parentId") Long parentId
	);

	@Query("""
				SELECT l
					FROM Location l
					JOIN Tool t on t.location = l
					WHERE t.id = :toolId
					AND l.type = 'LINE'
		""")
	Location findByToolIdAndType(@Param("toolId") Long toolId);

	@Query("""
		SELECT l
			FROM Location l
			LEFT JOIN FETCH l.parent gl 
			WHERE l.name = :name 
			AND l.parent.id = :parentId	""")
	Optional<Location> findByNameAndParentId(
		@Param("name") String name,
		@Param("parentId") Long parentId
	);
}
