package com.nextorm.portal.service.dashboard;

import com.nextorm.portal.common.exception.dashboard.DashboardNotFoundException;
import com.nextorm.portal.common.exception.dashboard.DashboardWidgetNotFoundException;
import com.nextorm.portal.dto.dashboard.*;
import com.nextorm.portal.entity.dashboard.Dashboard;
import com.nextorm.portal.entity.dashboard.DashboardWidget;
import com.nextorm.portal.repository.dashboard.DashboardRepository;
import com.nextorm.portal.repository.dashboard.DashboardWidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {
	private final DashboardRepository dashboardRepository;
	private final DashboardWidgetRepository dashboardWidgetRepository;

	public List<DashboardListDto> getDashboards(Boolean isHide) {
		List<Dashboard> dashboards = isHide != null
									 ? dashboardRepository.findAllByIsHide(isHide)
									 : dashboardRepository.findAll();

		return dashboards.stream()
						 .map(DashboardListDto::from)
						 .sorted((o1, o2) -> {
							 if (o1.getSort() == null && o2.getSort() == null) {
								 return 0;
							 }
							 if (o1.getSort() == null) {
								 return 1;
							 }
							 if (o2.getSort() == null) {
								 return -1;
							 }
							 return Integer.compare(o1.getSort(), o2.getSort());
						 })
						 .collect(Collectors.toList());
	}

	public DashboardResponseDto getDashboard(Long dashboardId) {
		Optional<Dashboard> dashboard = dashboardRepository.findById(dashboardId);
		if (dashboard.isPresent()) {
			return DashboardResponseDto.from(dashboard.get());
		}
		throw new IllegalArgumentException("dashboard를 찾을 수 없습니다. id: " + dashboardId);
	}

	public DashboardResponseDto createDashboard(DashboardCreateRequestDto dashboardCreateCreateDto) {
		Dashboard createdDashboard = dashboardRepository.save(dashboardCreateCreateDto.toEntity());
		return DashboardResponseDto.from(createdDashboard);
	}

	public DashboardResponseDto modifyDashboard(
		Long id,
		DashboardUpdateRequestDto dashboardUpdateRequestDto
	) {
		Dashboard dashboard = dashboardRepository.findById(id)
												 .orElseThrow();
		dashboard.modify(dashboardUpdateRequestDto.toEntity());

		return DashboardResponseDto.from(dashboard);
	}

	public void deleteDashboard(Long id) {
		Optional<Dashboard> existDashboard = dashboardRepository.findById(id);
		if (existDashboard.isEmpty()) {
			throw new IllegalArgumentException("not exist Location!");
		}
		for (DashboardWidget widget : existDashboard.get()
													.getWidgets()) {
			dashboardWidgetRepository.deleteById(widget.getId());
		}
		dashboardRepository.deleteById(id);
	}

	public void updateDashboardSortAndIsHide(List<DashboardUpdateRequestDto> dashboards) {
		dashboards.forEach(updateDto -> {
			Dashboard dashboard = dashboardRepository.findById(updateDto.getId())
													 .orElseThrow(DashboardNotFoundException::new);

			Dashboard updateSortAndIsHide = updateDto.toEntity();
			dashboard.modify(updateSortAndIsHide.getSort(), updateSortAndIsHide.isHide());
		});
	}

	public void updateDashboardWidget(
		Long widgetId,
		DashboardWIdgetUpdateRequestDto updateRequestDto
	) {
		dashboardWidgetRepository.findById(widgetId)
								 .orElseThrow(DashboardWidgetNotFoundException::new)
								 .modifyConfig(updateRequestDto.getConfig());
	}
}
