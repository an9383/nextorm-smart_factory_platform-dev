package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.OcapAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OcapAlarmRepository extends JpaRepository<OcapAlarm, Long> {
	String SELECT_WITH_FETCH = """
		SELECT oa
		          FROM OcapAlarm oa
		          LEFT JOIN FETCH oa.tool
		          LEFT JOIN FETCH oa.parameter
		          LEFT JOIN FETCH oa.alarmIntervalCode
		""";

	@Query(SELECT_WITH_FETCH)
	List<OcapAlarm> findAllWithFetch();

	@Query(SELECT_WITH_FETCH + " where oa.id = :id")
	Optional<OcapAlarm> findByIdWithFetch(@Param("id") Long id);

	@Query(SELECT_WITH_FETCH + " where oa.parameter.id = :parameterId")
	List<OcapAlarm> findAllByParameterIdWithFetch(@Param(("parameterId")) Long parameterId);
}