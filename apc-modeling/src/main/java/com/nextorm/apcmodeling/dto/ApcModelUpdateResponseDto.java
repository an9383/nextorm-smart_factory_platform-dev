package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApcModelUpdateResponseDto {
	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;
	private Long activeModelVersionId;
	private String modelName;
	private String description;

	public static ApcModelUpdateResponseDto from(
		boolean isUse,
		String modelName,
		boolean isUseNotify,
		Long activeVersionId,
		String description
	) {
		return ApcModelUpdateResponseDto.builder()
										.isUse(isUse)
										.modelName(modelName)
										.isUseNotify(isUseNotify)
										.activeModelVersionId(activeVersionId)
										.description(description)
										.build();
	}
}
