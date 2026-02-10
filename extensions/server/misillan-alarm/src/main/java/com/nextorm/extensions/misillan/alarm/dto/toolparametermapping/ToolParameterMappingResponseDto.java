package com.nextorm.extensions.misillan.alarm.dto.toolparametermapping;

import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolParameterMappingResponseDto {

	private Long id;
	private Long toolId;
	private String toolName;
	private String parameterName;
	private String conditionType;

	public static ToolParameterMappingResponseDto from(ToolParameterMapping mapping) {
		return ToolParameterMappingResponseDto.builder()
											  .id(mapping.getId())
											  .toolId(mapping.getTool()
															 .getId())
											  .toolName(mapping.getTool()
															   .getName())
											  .parameterName(mapping.getParameter()
																	.getName())
											  .conditionType(String.valueOf(mapping.getConditionType()))
											  .build();
	}

}
