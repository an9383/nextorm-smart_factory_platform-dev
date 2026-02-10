package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcModelSimulationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApcModelSimulationDataRepository extends JpaRepository<ApcModelSimulationData, Long> {
	Optional<ApcModelSimulationData> findByApcRequestId(@Param("apcRequestId") Long apcRequestId);
}