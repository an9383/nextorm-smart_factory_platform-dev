package com.nextorm.portal.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModelBuildRequestEvent {
	private Long modelId;
}
