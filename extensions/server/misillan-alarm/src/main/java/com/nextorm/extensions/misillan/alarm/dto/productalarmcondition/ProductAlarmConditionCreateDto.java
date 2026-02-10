package com.nextorm.extensions.misillan.alarm.dto.productalarmcondition;

import com.nextorm.extensions.misillan.alarm.entity.EqmsProduct;
import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAlarmConditionCreateDto {
	private Long productId;
	private Double temperature;
	private Double pressure;

	public static ProductAlarmCondition toEntity(EqmsProduct eqmsProduct,ProductAlarmConditionCreateDto dto) {
		return ProductAlarmCondition.builder()
				.product(eqmsProduct)
				.temperature(dto.getTemperature())
				.pressure(dto.getPressure())
				.build();
	}
}
