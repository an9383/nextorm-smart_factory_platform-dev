package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

	Optional<Rule> findByName(@Param("name") String name);

	//    List<Rule> findByDcpConfigIdIn(List<Long> dcpConfigIds);

}
