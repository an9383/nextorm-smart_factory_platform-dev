package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.DcpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DcpConfigRepository extends JpaRepository<DcpConfig, Long> {
	List<DcpConfig> findByToolId(@Param("toolId") Long toolId);

	@Query("""
		SELECT distinct dcp 
			FROM DcpConfig dcp 
			LEFT JOIN FETCH dcp.parameterMappings pm
			LEFT JOIN FETCH pm.parameter
			WHERE dcp.tool.id = :toolId
		""")
	List<DcpConfig> findByToolIdWithParameters(@Param("toolId") Long toolId);

	List<DcpConfig> findByToolIdIn(@Param("toolIds") List<Long> toolIds);

	@Query("""
		SELECT distinct dcp 
			FROM DcpConfig dcp 
			LEFT JOIN FETCH dcp.parameterMappings pm
			LEFT JOIN FETCH pm.parameter
		""")
	List<DcpConfig> findAllWithParameters();

	@Query("""
		SELECT distinct dcp
			FROM DcpConfig dcp
			LEFT JOIN dcp.ruleMappings rm
			WHERE rm.rule.id = :ruleId
		""")
	List<DcpConfig> findAllByRuleId(@Param("ruleId") Long ruleId);

	@Query("""
			SELECT dcp
				FROM DcpConfig dcp
				LEFT JOIN dcp.parameterMappings pm
				WHERE pm.parameter.id = :parameterId
		""")
	List<DcpConfig> findAllByParameterId(@Param("parameterId") Long parameterId);

	@Modifying
	@Query("""
					UPDATE DcpConfig dcp 
						SET dcp.lastCollectedAt = :lastTraceAt 
					WHERE dcp.id = :dcpId
		""")
	void updateLastCollectedAtById(
		@Param("dcpId") Long dcpId,
		@Param("lastTraceAt") LocalDateTime lastTraceAt
	);
}
