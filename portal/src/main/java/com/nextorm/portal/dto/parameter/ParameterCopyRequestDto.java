package com.nextorm.portal.dto.parameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterCopyRequestDto {
	private List<Long> targetToolIds;
	private List<ParameterCreateRequestDto> parameterCreateRequestDtos;
}
