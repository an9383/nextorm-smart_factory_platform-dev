package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolCreateRequestDto {
	private Long locationId;
	private String name;
	private Tool.Type type;
	private Tool.ToolType toolType;

	public Tool toEntity(Location location) {
		return Tool.builder()
				   .location(location)
				   .name(name)
				   .type(type)
				   .toolType(toolType)
				   .build();
	}
}
