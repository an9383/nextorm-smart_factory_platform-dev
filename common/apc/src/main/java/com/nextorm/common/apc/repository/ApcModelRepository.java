package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApcModelRepository extends JpaRepository<ApcModel, Long> {
	ApcModel findByModelNameAndIsDelete(
		String modelName,
		boolean isDelete
	);

	@Query("""
		SELECT m
			FROM ApcModel m
			WHERE m.isUse = true
				AND m.isDelete = false
		""")
	List<ApcModel> findAllActiveModels();
}