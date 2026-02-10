package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.CollectorDefine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectorDefineRepository extends JpaRepository<CollectorDefine, Long> {
	Optional<CollectorDefine> findFirstBy();
}