package com.nextorm.portal.repository.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
	List<Dashboard> findAllByIsHide(@Param("isHide") Boolean isHide);
}
