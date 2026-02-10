package com.nextorm.extensions.misillan.alarm.dto.productalarmcondition;

import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAlarmConditionResponseDto {
	private Long id;
	private Long productId;
	private String productName;
	private Double temperature;
	private Double pressure;

	public static ProductAlarmConditionResponseDto from(ProductAlarmCondition productAlarmCondition) {
		return ProductAlarmConditionResponseDto.builder()
											   .id(productAlarmCondition.getId())
											   .productId(productAlarmCondition.getProduct()
																			   .getId())
											   .productName(productAlarmCondition.getProduct()
																				 .getName())
											   .temperature(productAlarmCondition.getTemperature())
											   .pressure(productAlarmCondition.getPressure())
											   .build();
	}
}
