package com.nextorm.extensions.misillan.alarm.repository;

import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductAlarmConditionRepository extends JpaRepository<ProductAlarmCondition, Long> {
	@Query("""
		select pac
		from ProductAlarmCondition pac
		left join fetch pac.product p
		order by pac.id desc
		""")
	List<ProductAlarmCondition> findAllWithProduct();

	@Query("""
		select pac
		from ProductAlarmCondition pac
		left join fetch pac.product p
		where p.id = :id
		""")
	Optional<ProductAlarmCondition> findByProductId(@Param("id") Long id);
}