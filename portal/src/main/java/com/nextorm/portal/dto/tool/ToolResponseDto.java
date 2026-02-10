package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.portal.dto.location.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ToolResponseDto {
	private LocationDto location;
	private String name;
	private Tool.Type type;
	private Tool.ToolType toolType;
	private Long parentId;
	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ToolResponseDto from(Tool entity) {
		if (entity == null) {
			return null;
		}

		return ToolResponseDto.builder()
							  .id(entity.getId())
							  .location(entity.getLocation() != null
										? LocationDto.of(entity.getLocation())
										: null)
							  .name(entity.getName())
							  .type(entity.getType())
							  .toolType(entity.getToolType())
							  .createAt(entity.getCreateAt())
							  .createBy(entity.getCreateBy())
							  .updateAt(entity.getUpdateAt())
							  .updateBy(entity.getUpdateBy())
							  .parentId(entity.getParentId())
							  .build();
	}
}
