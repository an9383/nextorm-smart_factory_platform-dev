package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ParameterExtraData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParameterExtraDataRepository extends JpaRepository<ParameterExtraData, Long> {
	List<ParameterExtraData> findByParameterIdIn(@Param("parameterIds") List<Long> parameterIds);

	@Query("""
			SELECT ped
				FROM Parameter p
				LEFT JOIN ParameterExtraData ped
					ON p.id = ped.parameterId
				WHERE p.tool.id = :toolId
					AND p.name = :parameterName
		""")
	Optional<ParameterExtraData> findByToolIdAndParameterName(
		@Param("toolId") Long toolId,
		@Param("parameterName") String parameterName
	);
}