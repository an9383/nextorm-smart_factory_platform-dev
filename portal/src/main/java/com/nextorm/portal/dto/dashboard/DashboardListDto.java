package com.nextorm.portal.dto.dashboard;

import com.nextorm.portal.entity.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardListDto {

	private String name;
	private Long id;
	private Integer sort;
	private Boolean isHide;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static DashboardListDto from(Dashboard entity) {
		if (entity == null) {
			return null;
		}

		return DashboardListDto.builder()
							   .name(entity.getName())
							   .id(entity.getId())
							   .sort(entity.getSort())
							   .isHide(entity.isHide())
							   .createAt(entity.getCreateAt())
							   .createBy(entity.getCreateBy())
							   .updateAt(entity.getUpdateAt())
							   .updateBy(entity.getUpdateBy())
							   .build();
	}

}
