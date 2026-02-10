package com.nextorm.extensions.misillan.alarm.dto.sfp;

import com.nextorm.extensions.misillan.alarm.entity.SfpParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SfpParameterResponseDto {

	private Long id;
	private String name;

	public static SfpParameterResponseDto from(SfpParameter sfpParameter) {
		return SfpParameterResponseDto.builder()
									  .id(sfpParameter.getId())
									  .name(sfpParameter.getName())
									  .build();
	}
}
