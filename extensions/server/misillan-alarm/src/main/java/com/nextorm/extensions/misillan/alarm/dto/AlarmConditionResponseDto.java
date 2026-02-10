package com.nextorm.extensions.misillan.alarm.dto;

import com.nextorm.extensions.misillan.alarm.entity.AlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ConditionType;
import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmConditionResponseDto {
	private Long id;
	private Long toolParameterMappingId;
	private Long toolId;
	private String toolName;
	private Long parameterId;
	private String parameterName;
	private Long productAlarmConditionId;
	private Long productId;
	private String productName;
	private ConditionType conditionType;
	private String displayName;
	private Double temperature;
	private Double pressure;
	private Boolean isActive;
	private LocalDateTime activeTime;
	private Long timer;

	public static AlarmConditionResponseDto from(AlarmCondition condition) {
		ToolParameterMapping toolParameterMapping = condition.getToolParameterMapping();
		ProductAlarmCondition productAlarmCondition = condition.getProductAlarmCondition();

		return AlarmConditionResponseDto.builder()
										.id(condition.getId())
										.conditionType(toolParameterMapping.getConditionType())
										.displayName(toolParameterMapping.getConditionType()
																		 .getDisplayName())
										.toolParameterMappingId(toolParameterMapping.getId())
										.toolId(toolParameterMapping.getTool()
																	.getId())
										.toolName(toolParameterMapping.getTool()
																	  .getName())
										.parameterId(toolParameterMapping.getParameter()
																		 .getId())
										.parameterName(toolParameterMapping.getParameter()
																		   .getName())
										.productAlarmConditionId(productAlarmCondition != null
																 ? productAlarmCondition.getId()
																 : null)
										.productId(productAlarmCondition != null && productAlarmCondition.getProduct() != null
												   ? productAlarmCondition.getProduct()
																		  .getId()
												   : null)
										.productName(productAlarmCondition != null && productAlarmCondition.getProduct() != null
													 ? productAlarmCondition.getProduct()
																			.getName()
													 : null)
										.temperature(condition.getTemperature())
										.pressure(condition.getPressure())
										.isActive(condition.isActive())
										.activeTime(condition.getActiveTime())
										.timer(condition.getTimer())
										.build();
	}

}
