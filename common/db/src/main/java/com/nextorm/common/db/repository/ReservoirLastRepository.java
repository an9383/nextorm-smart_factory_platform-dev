package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ReservoirLast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservoirLastRepository extends JpaRepository<ReservoirLast,Long> {
}
