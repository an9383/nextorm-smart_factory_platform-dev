package com.nextorm.extensions.misillan.alarm.dto.toolparametermapping;

import com.nextorm.extensions.misillan.alarm.entity.ConditionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolParameterMappingCreateDto {

	private Long toolId;
	private Long parameterId;
	private ConditionType conditionType;
}
