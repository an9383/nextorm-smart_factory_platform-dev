package com.nextorm.portal.repository.system;

import com.nextorm.portal.entity.system.SystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemInfoRepository extends JpaRepository<SystemInfo, Long> {
	List<SystemInfo> findByKeyIn(List<String> systemKeys);
}