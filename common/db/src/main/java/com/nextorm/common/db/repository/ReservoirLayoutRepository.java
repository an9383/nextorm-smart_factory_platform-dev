package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ReservoirLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservoirLayoutRepository extends JpaRepository<ReservoirLayout, Long> {
	List<ReservoirLayout> findByToolId(@Param("toolId") Long toolId);

	List<ReservoirLayout> findByToolIdIn(@Param("toolIds") List<Long> toolIds);
}
