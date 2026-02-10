package com.nextorm.portal.dto.parameter;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.portal.dto.location.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterToolResponseDto {
	private Long id;
	private String name;

	private Long toolId;
	private String toolName;
	private LocationDto location;

	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ParameterToolResponseDto from(Parameter entity) {
		if (entity == null) {
			return null;
		}

		Tool tool = entity.getTool();

		return ParameterToolResponseDto.builder()
									   .id(entity.getId())
									   .name(entity.getName())
									   .toolId(tool != null
											   ? tool.getId()
											   : null)
									   .toolName(tool != null
												 ? tool.getName()
												 : null)
									   .location(tool.getLocation() != null
												 ? LocationDto.of(tool.getLocation())
												 : null)
									   .createAt(entity.getCreateAt())
									   .createBy(entity.getCreateBy())
									   .updateAt(entity.getUpdateAt())
									   .updateBy(entity.getUpdateBy())
									   .build();
	}
}
