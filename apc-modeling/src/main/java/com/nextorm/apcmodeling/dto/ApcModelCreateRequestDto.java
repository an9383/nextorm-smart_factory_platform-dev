package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.apc.entity.ApcModel;
import com.nextorm.common.apc.entity.ApcModelVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApcModelCreateRequestDto {
	private String combineValue;
	private String formulaScript;
	private String formulaWorkspace;
	private String modelName;
	private String description;
	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isActive")
	private boolean isActive;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;

	public ApcModel toEntity() {
		return ApcModel.builder()
					   .modelName(modelName)
					   .condition(combineValue)
					   .isUse(isUse)
					   .build();
	}

	public ApcModelVersion toVersionEntity(
		ApcModel apcModel
	) {
		return ApcModelVersion.builder()
							  .apcModel(apcModel)
							  .version(1)
							  .formulaScript(formulaScript)
							  .formulaWorkspace(formulaWorkspace)
							  .description(description)
							  .isActive(isActive)
							  .isUseNotify(isUseNotify)
							  .build();
	}
}
