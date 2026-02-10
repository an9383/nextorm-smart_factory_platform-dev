package com.nextorm.portal.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessOptimizationRequestEvent {
	private Long optimizationId;
}
