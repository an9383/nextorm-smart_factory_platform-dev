package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.template.ParameterDataTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParameterDataRepository
	extends JpaRepository<ParameterData, Long>, ParameterDataQueryDsl, ParameterDataTemplateRepository {

	List<ParameterData> findByParameterIdAndTraceAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		Sort sort
	);

	List<ParameterData> findByParameterIdInAndTraceAtBetween(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		Sort sort
	);

	List<ParameterData> findByParameterIdInAndTraceAtGreaterThanEqualAndTraceAtLessThan(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		Sort sort
	);

	@Query("""
			  SELECT pd
			  	FROM ParameterData pd
			  	WHERE pd.parameterId = :parameterId
				ORDER BY pd.traceAt DESC
				LIMIT :limit
		""")
	List<ParameterData> findLimitRecentByParameterIdOrderByTraceAtDesc(
		@Param("parameterId") Long parameterId,
		@Param("limit") Integer limit
	);

	@Query("""
			  SELECT pd
			  	FROM ParameterData pd
			  	WHERE pd.id > :id 
			  		AND pd.parameterId = :parameterId
				ORDER BY pd.traceAt DESC
				LIMIT :limit
		""")
	List<ParameterData> findLimitRecentByIdGreaterThanAndParameterIdOrderByTraceAtDesc(
		@Param("id") Long id,
		@Param("parameterId") Long parameterId,
		@Param("limit") Integer limit
	);

	@Query("""
			DELETE
				FROM ParameterData pd
				WHERE pd.parameterId = :parameterId
					AND pd.traceAt BETWEEN :from AND :to
		""")
	@Modifying(clearAutomatically = true)
	int deleteAllByParameterIdAndTraceAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	List<ParameterData> findLatestParameterData(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("baseDataTime") LocalDateTime baseDateTime
	);

	@Query("""
			SELECT pd 
			FROM ParameterData pd 
			WHERE pd.parameterId = (
				SELECT p.id 
				FROM Parameter p 
				WHERE p.tool.id = :toolId 
					AND p.dataType = 'IMAGE'
				) 
			AND pd.traceAt <= :dateTime 
			ORDER BY pd.traceAt DESC 
			LIMIT 1
		""")
	ParameterData findImageByToolIdAndTraceAt(
		@Param("toolId") Long toolId,
		@Param("dateTime") LocalDateTime dateTime
	);

	@Query("""
			SELECT DISTINCT pd.sValue 
			FROM ParameterData pd 
			WHERE pd.parameterId = :parameterId 
				AND pd.traceAt BETWEEN :from AND :to
		""")
	List<String> findDistinctStringValueByParameterIdAndTraceAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	@Query("""
			SELECT pd 
			FROM ParameterData pd 
			WHERE pd.parameterId = :parameterId 
				AND pd.traceAt = :traceAt 
				AND pd.sValue = :sValue
		""")
	Optional<ParameterData> findByParameterIdAndTraceAtAndSValue(
		@Param("parameterId") Long parameterId,
		@Param("traceAt") LocalDateTime traceAt,
		@Param("sValue") String sValue
	);

	Page<ParameterData> findAllByParameterIdAndTraceAtBetween(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		Pageable pageable
	);

	long countByParameterIdInAndTraceAtBetween(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	@Query("""
			SELECT pd 
			FROM ParameterData pd 
			WHERE pd.parameterId IN :parameterIds 
				AND pd.traceAt BETWEEN :from AND :to
		""")
	List<ParameterData> findUnsortedByParameterIdInAndTraceAtBetween(
		@Param("parameterIds") List<Long> parameterIds,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);

	Optional<ParameterData> findFirstByParameterIdAndTraceAtAfterOrderByTraceAtDesc(
		@Param("parameterId") Long parameterId,
		@Param("from") LocalDateTime from
	);
}
