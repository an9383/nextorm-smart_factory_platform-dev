package com.nextorm.portal.client.modelbuild;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelBuildRequest {
	private String site;
	private Long modelId;
}
