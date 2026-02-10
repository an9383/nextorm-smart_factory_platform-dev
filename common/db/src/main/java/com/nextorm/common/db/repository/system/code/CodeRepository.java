package com.nextorm.common.db.repository.system.code;

import com.nextorm.common.db.entity.system.code.Code;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Long> {

	@Query("""
		SELECT c
		  FROM Code c
		  LEFT JOIN FETCH c.category
		""")
	List<Code> findAllWithCategory(Sort sort);

	@Query("""
		SELECT DISTINCT c
		  FROM Code c
		  LEFT JOIN FETCH c.category
		  LEFT JOIN FETCH c.childCategory
		  LEFT JOIN FETCH c.childCodes cc
		  LEFT JOIN FETCH cc.child ch
		  LEFT JOIN FETCH ch.category
		  LEFT JOIN FETCH c.parentCodes pc
		  LEFT JOIN FETCH pc.parent pa
		  LEFT JOIN FETCH pa.category
		 WHERE c.id = :id
		""")
	Optional<Code> findByIdWithCategory(@Param("id") Long id);

	@Query("""
		SELECT c
		  FROM Code c
		  JOIN FETCH c.category cat
		 WHERE cat.category = :category
		 ORDER BY c.sort
		""")
	List<Code> findByCategory(@Param("category") String category);

	boolean existsByCategoryIdAndCode(
		Long categoryId,
		String code
	);

	@Query("""
		SELECT c
		  FROM Code c
		  JOIN FETCH c.category cat
		 WHERE cat.id = :categoryId
		   AND c.id IN (:codeIds)
		 ORDER BY c.sort
		""")
	List<Code> findByCategoryIdAndIds(
		@Param("categoryId") Long categoryId,
		@Param("codeIds") List<Long> codeIds
	);
}
