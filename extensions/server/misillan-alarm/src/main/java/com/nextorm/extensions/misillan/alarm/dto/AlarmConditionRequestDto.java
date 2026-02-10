package com.nextorm.extensions.misillan.alarm.dto;

import com.nextorm.extensions.misillan.alarm.entity.AlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmConditionRequestDto {
	private Long toolParameterMappingId;
	private Long productAlarmConditionId;
	private Double temperature;
	private Double pressure;
	private boolean isActive;
	private Long timer;

	public AlarmCondition toEntity(
		ToolParameterMapping toolParameterMapping,
		ProductAlarmCondition productAlarmCondition
	) {
		return AlarmCondition.builder()
							 .toolParameterMapping(toolParameterMapping)
							 .productAlarmCondition(productAlarmCondition)
							 .temperature(temperature)
							 .pressure(pressure)
							 .timer(timer)
							 .isActive(false)
							 .build();
	}
}
