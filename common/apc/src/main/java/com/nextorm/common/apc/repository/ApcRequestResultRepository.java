package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcRequestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApcRequestResultRepository extends JpaRepository<ApcRequestResult, Long> {
	List<ApcRequestResult> findByApcRequestId(@Param("apcRequestId") Long apcRequestId);

	List<ApcRequestResult> findAllByApcRequestIdIn(List<Long> requestIds);
}