package com.nextorm.extensions.misillan.alarm.repository;

import com.nextorm.extensions.misillan.alarm.entity.AlarmCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlarmConditionRepository extends JpaRepository<AlarmCondition, Long> {
	@Query("""
			SELECT ac
				FROM AlarmCondition ac
				JOIN FETCH ac.toolParameterMapping tpm
				JOIN FETCH tpm.tool
				JOIN FETCH tpm.parameter
				JOIN FETCH ac.productAlarmCondition pac
				JOIN FETCH pac.product
		""")
	List<AlarmCondition> findAllWithFetch();

	@Query("""
		select ac
		from AlarmCondition ac
		left join fetch ac.toolParameterMapping tpm
		left join fetch tpm.tool t
		left join fetch tpm.parameter sp
		left join fetch ac.productAlarmCondition pac
		left join fetch pac.product p
		""")
	List<AlarmCondition> findAllWithToolParameterMappingAndProductAlarmCondition();

	@Override
	@Query("""
		select ac
		from AlarmCondition ac
		left join fetch ac.toolParameterMapping tpm
		left join fetch tpm.tool t
		left join fetch tpm.parameter sp
		left join fetch ac.productAlarmCondition pac
		left join fetch pac.product p
		where ac.id = :id
		""")
	Optional<AlarmCondition> findById(@Param("id") Long id);
}