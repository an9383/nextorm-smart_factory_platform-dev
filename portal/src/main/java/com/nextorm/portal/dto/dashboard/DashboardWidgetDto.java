package com.nextorm.portal.dto.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import com.nextorm.portal.entity.dashboard.DashboardWidget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWidgetDto {

	private Long id;
	private Long dashboardId;
	private String widgetId;
	private String title;
	private String config;
	private int x;
	private int y;
	private int w;
	private int h;

	public static DashboardWidgetDto from(DashboardWidget entity) {
		if (entity == null) {
			return null;
		}

		return DashboardWidgetDto.builder()
								 .id(entity.getId())
								 .dashboardId(entity.getDashboard() != null
											  ? entity.getDashboard()
													  .getId()
											  : null)
								 .widgetId(entity.getWidgetId())
								 .title(entity.getTitle())
								 .config(entity.getConfig())
								 .x(entity.getX())
								 .y(entity.getY())
								 .w(entity.getW())
								 .h(entity.getH())
								 .build();
	}

	public DashboardWidget toEntity() {

		return DashboardWidget.builder()
							  .id(id)
							  .dashboard(getDashboardId() != null
										 ? Dashboard.builder()
													.id(getDashboardId())
													.build()
										 : null)
							  .widgetId(widgetId)
							  .title(title)
							  .config(config)
							  .x(x)
							  .y(y)
							  .w(w)
							  .h(h)
							  .build();
	}

	public DashboardWidget toEntity(Dashboard dashboard) {
		DashboardWidget dashboardWidget = DashboardWidget.builder()
														 .id(id)
														 .dashboard(dashboard)
														 .widgetId(widgetId)
														 .title(title)
														 .config(config)
														 .x(x)
														 .y(y)
														 .w(w)
														 .h(h)
														 .build();

		// 대시보드와 위젯 간의 양방향 관계 설정
		dashboard.getWidgets()
				 .add(dashboardWidget);

		return dashboardWidget;
	}
}
