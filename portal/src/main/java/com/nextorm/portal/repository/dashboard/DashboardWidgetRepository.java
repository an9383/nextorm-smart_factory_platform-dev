package com.nextorm.portal.repository.dashboard;

import com.nextorm.portal.entity.dashboard.DashboardWidget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, Long> {
}
