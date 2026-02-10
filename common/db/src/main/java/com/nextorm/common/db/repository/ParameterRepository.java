package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Parameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long>, ParameterQueryDsl {
	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool t WHERE t.id = :toolId ORDER BY :#{#sort}")
	List<Parameter> findByToolId(
		@Param("toolId") Long toolId,
		Sort sort
	);

	List<Parameter> findByToolId(@Param("toolId") Long toolId);

	List<Parameter> findByToolIdAndDataType(
		@Param("toolId") Long toolId,
		@Param("dataType") Parameter.DataType dataType
	);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool ORDER BY :#{#sort}")
	List<Parameter> findAll(Sort sort);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool WHERE p.id IN :ids")
	List<Parameter> findByIdIn(@Param("ids") List<Long> ids);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool WHERE p.id = :id")
	Optional<Parameter> findById(@Param("id") Long id);

	Optional<Parameter> findByToolIdAndName(
		@Param("toolId") Long toolId,
		@Param("name") String name
	);

	@Query("""
			SELECT distinct p
				FROM Parameter p
				LEFT JOIN FETCH p.mappingParameters pmp
				LEFT JOIN FETCH pmp.parameter
				WHERE p.id = :id
		""")
	Optional<Parameter> findByIdWithMappingParameters(@Param("id") Long id);

	@Query("""
		SELECT distinct p
			FROM Parameter p
			LEFT JOIN DcpConfigParameterMapping dcpm
				ON p.id = dcpm.parameter.id
			WHERE dcpm.dcpConfig.id IN :dcpConfigIds
		""")
	List<Parameter> findDistanceAllByDcpConfigIds(@Param("dcpConfigIds") List<Long> dcpConfigIds);

	List<Parameter> findByToolIdIn(@Param("toolIds") List<Long> toolIds);

	@Query("""
		SELECT p
			FROM Parameter p
			LEFT JOIN FETCH p.tool t 
			LEFT JOIN FETCH t.location l 
			LEFT JOIN FETCH l.parent lp 
			LEFT JOIN FETCH lp.parent lgp 
			WHERE p.id IN :ids
		""")
	List<Parameter> findByIdInWithToolLocation(@Param("ids") List<Long> ids);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool WHERE p.id = :id AND p.type = :type")
	Optional<Parameter> findByIdAndType(
		@Param("id") long id,
		@Param("type") Parameter.Type type
	);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool WHERE p.id IN :parameterIds")
	List<Parameter> findAllByIdInWithTool(
		@Param("parameterIds") List<Long> parameterIds
	);

	@Query("SELECT p FROM Parameter p LEFT JOIN FETCH p.tool WHERE p.id IN :ids AND p.type = :type")
	List<Parameter> findByIdInAndType(
		@Param("ids") List<Long> ids,
		@Param("type") Parameter.Type type
	);
}
