package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolUpdateRequestDto {
	private Long locationId;
	private String name;
	private Tool.Type type;
	private Tool.ToolType toolType;

	public Tool toEntity() {
		return Tool.builder()
				   .name(name)
				   .type(type)
				   .toolType(toolType)
				   .build();
	}
}
