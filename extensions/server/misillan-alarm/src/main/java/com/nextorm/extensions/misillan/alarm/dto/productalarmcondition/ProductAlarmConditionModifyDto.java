package com.nextorm.extensions.misillan.alarm.dto.productalarmcondition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAlarmConditionModifyDto {

	private Long productId;
	private Double temperature;
	private Double pressure;
}
