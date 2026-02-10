package com.nextorm.extensions.misillan.alarm.cdc;

import com.nextorm.extensions.misillan.alarm.entity.*;

import java.time.LocalDateTime;

record DetectCondition(Long parameterId, String parameterName, Double pressure, Double temperature,
					   ConditionType conditionType, Long toolId, String toolName, Long productId, String productName,
					   LocalDateTime activeAt, Long timerSeconds) {

	public static DetectCondition of(AlarmCondition entity) {
		ToolParameterMapping toolParameterMapping = entity.getToolParameterMapping();
		SfpParameter parameter = toolParameterMapping.getParameter();
		EqmsTool tool = toolParameterMapping.getTool();
		EqmsProduct product = entity.getProductAlarmCondition()
									.getProduct();

		return new DetectCondition(parameter.getId(),
			parameter.getName(),
			entity.getPressure(),
			entity.getTemperature(),
			toolParameterMapping.getConditionType(),
			tool.getId(),
			tool.getName(),
			product.getId(),
			product.getName(),
			entity.getActiveTime(),
			entity.getTimer());
	}

}