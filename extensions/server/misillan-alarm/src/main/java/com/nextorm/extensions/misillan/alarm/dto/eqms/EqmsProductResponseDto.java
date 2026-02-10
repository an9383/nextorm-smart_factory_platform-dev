package com.nextorm.extensions.misillan.alarm.dto.eqms;

import com.nextorm.extensions.misillan.alarm.entity.EqmsProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EqmsProductResponseDto {

	private Long id;
	private String name;


	public static EqmsProductResponseDto from(EqmsProduct eqmsProduct) {
		return EqmsProductResponseDto.builder()
				.id(eqmsProduct.getId())
				.name(eqmsProduct.getName())
				.build();
	}
}
