package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.entity.ApcRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApcRequestRepository extends JpaRepository<ApcRequest, Long>, ApcRequestQueryDsl {
}