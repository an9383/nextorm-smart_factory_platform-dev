package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ToolKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToolKafkaRepository extends JpaRepository<ToolKafka, Long> {
	Optional<ToolKafka> findByToolId(@Param("toolId") Long toolId);
}
