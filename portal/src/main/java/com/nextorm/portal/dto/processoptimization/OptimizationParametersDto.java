package com.nextorm.portal.dto.processoptimization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptimizationParametersDto {
	private Long parameterId;
	private Double minScaleX;
	private Double maxScaleX;
	private Integer step;
}
