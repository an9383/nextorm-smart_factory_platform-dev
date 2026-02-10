package com.nextorm.extensions.misillan.alarm.dto.eqms;

import com.nextorm.extensions.misillan.alarm.entity.EqmsTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EqmsToolResponseDto {
	private Long id;
	private String name;

	public static EqmsToolResponseDto from(EqmsTool eqmsTool) {
		return EqmsToolResponseDto.builder()
				.id(eqmsTool.getId())
				.name(eqmsTool.getName())
				.build();
	}
}
