package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApcModelVersionRepository extends JpaRepository<ApcModelVersion, Long> {
	@Query("""
			SELECT mv
				FROM ApcModelVersion mv
				WHERE mv.apcModel.id = :apcModelId
					AND mv.isActive is true
		""")
	ApcModelVersion findActiveVersionByModelId(@Param("apcModelId") Long apcModelId);

	@Query("""
			SELECT mv
				FROM ApcModelVersion mv
				WHERE mv.apcModel.id = :apcModelId
		""")
	List<ApcModelVersion> findAllVersionByApcModelId(
		@Param("apcModelId") Long apcModelId
	);

	@Query("""
			SELECT MAX(mv.version) 
				FROM ApcModelVersion mv 
				WHERE mv.apcModel.id = :apcModelId 
		""")
	Integer findMaxVersionByApcModelId(
		@Param("apcModelId") Long apcModelId
	);

	@Query("""
			SELECT mv
				FROM ApcModelVersion mv
				LEFT JOIN FETCH mv.apcModel m
				WHERE mv.id = :apcModelVersionId
		""")
	ApcModelVersion findByApcModelVersionId(
		@Param("apcModelVersionId") Long apcModelVersionId
	);

	@Query("""
				SELECT mv
					FROM  ApcModelVersion mv
					LEFT JOIN FETCH mv.apcModel m
					WHERE m.isDelete is false
						AND mv.isActive is true
						ORDER BY CASE WHEN m.isUse = true THEN 0 ELSE 1 END, m.createAt DESC 
		""")
	List<ApcModelVersion> findAllActiveApcModelVersion();

	@Query("""
		SELECT mv
		     FROM ApcModelVersion mv
		     LEFT JOIN FETCH mv.apcModel
		    WHERE mv.id = :id
		""")
	@Override
	Optional<ApcModelVersion> findById(@Param("id") Long id);
}