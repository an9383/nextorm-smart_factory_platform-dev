package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ReservoirCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservoirCapacityRepository extends JpaRepository<ReservoirCapacity, Long>, ReservoirCapacityQueryDsl {

}
