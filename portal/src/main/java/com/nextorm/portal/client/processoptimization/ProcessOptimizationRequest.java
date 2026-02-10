package com.nextorm.portal.client.processoptimization;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessOptimizationRequest {
	private String site;
	private Long optimizationId;
}
