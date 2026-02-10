package com.nextorm.portal.entity.processoptimization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OptimizationParameters {
	private Long parameterId;
	private Double minScaleX;
	private Double maxScaleX;
	private Integer step;
}
