package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ai.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiModelRepository extends JpaRepository<AiModel, Long> {
	List<AiModel> findByStatus(AiModel.Status status);

	List<AiModel> findByToolId(Long toolId);
}