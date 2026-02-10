package com.nextorm.common.db.repository;

import com.nextorm.common.db.entity.ToolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolStatusRepository extends JpaRepository<ToolStatus, Long> {
	List<ToolStatus> findByToolId(Long toolId);
}
