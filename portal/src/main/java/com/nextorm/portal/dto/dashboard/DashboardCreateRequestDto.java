package com.nextorm.portal.dto.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCreateRequestDto {

	private String name;

	private List<DashboardWidgetDto> widgets = new ArrayList<>();

	public Dashboard toEntity() {
		Dashboard dashboard = Dashboard.builder()
									   .name(name)
									   .isHide(false)
									   .widgets(new ArrayList<>()) // 빈 리스트로 초기화
									   .build();
		widgets.forEach(dashboardWidgetDto -> dashboard.getWidgets()
													   .add(dashboardWidgetDto.toEntity(dashboard)));

		return dashboard;
	}

}
