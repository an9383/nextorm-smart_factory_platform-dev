package com.nextorm.extensions.misillan.alarm.repository;

import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ToolParameterMappingRepository extends JpaRepository<ToolParameterMapping, Long> {
	@Query("""
		select tpm
		from ToolParameterMapping tpm
		left join fetch tpm.tool t
		left join fetch tpm.parameter sp
		order by tpm.id desc
		""")
	List<ToolParameterMapping> findAllWithToolAndParameter();

	@Query("""
		select tpm
		from ToolParameterMapping tpm
		left join fetch tpm.tool t
		left join fetch tpm.parameter sp
		where t.id = :id
		""")
	Optional<ToolParameterMapping> findByToolId(@Param("id") Long id);
}