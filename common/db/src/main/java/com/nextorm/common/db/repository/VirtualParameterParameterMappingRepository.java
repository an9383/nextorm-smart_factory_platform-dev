package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.VirtualParameterParameterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualParameterParameterMappingRepository
	extends JpaRepository<VirtualParameterParameterMapping, Long> {

	@Query("""
			SELECT distinct vppm.virtualParameter
				FROM VirtualParameterParameterMapping vppm
				WHERE vppm.parameter.id IN :parameterIds
					AND vppm.isUsingCalculation = :isUsingCalculation
		""")
	List<Parameter> findAllByParameterIdAndIsUsingCalculation(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("isUsingCalculation") boolean isUsingCalculation
	);
}