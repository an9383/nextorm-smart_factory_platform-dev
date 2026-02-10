package com.nextorm.portal.dto.parameter;

import com.nextorm.common.db.entity.ParameterExtraData;
import lombok.Getter;

import java.util.Map;

@Getter
public class ParameterExtraDataResponseDto {
	private Long parameterId;
	private Map<String, Object> extraData;

	public static ParameterExtraDataResponseDto from(ParameterExtraData extraData) {
		ParameterExtraDataResponseDto dto = new ParameterExtraDataResponseDto();
		dto.parameterId = extraData.getParameterId();
		dto.extraData = extraData.getExtraData();
		return dto;
	}
}
