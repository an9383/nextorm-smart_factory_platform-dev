package com.nextorm.portal.dto.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

	private String name;

	private List<DashboardWidgetDto> widgets = new ArrayList<>();

	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static DashboardResponseDto from(Dashboard entity) {
		if (entity == null) {
			return null;
		}

		return DashboardResponseDto.builder()
								   .name(entity.getName())
								   .id(entity.getId())
								   .createBy(entity.getCreateBy())
								   .createAt(entity.getCreateAt())
								   .updateBy(entity.getUpdateBy())
								   .updateAt(entity.getUpdateAt())
								   .widgets(entity.getWidgets()
												  .stream()
												  .map(DashboardWidgetDto::from)
												  .collect(Collectors.toList()))
								   .build();
	}
}
