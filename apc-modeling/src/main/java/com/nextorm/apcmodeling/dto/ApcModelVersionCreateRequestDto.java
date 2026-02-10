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
public class ApcModelVersionCreateRequestDto {
	private String formulaScript;
	private String formulaWorkspace;
	private String modelName;
	private String description;

	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;
	@JsonProperty("isActive")
	private boolean isActive;

	public ApcModelVersion toVersionEntity(
		ApcModel apcModel,
		Integer version
	) {
		return ApcModelVersion.builder()
							  .apcModel(apcModel)
							  .version(version + 1)
							  .formulaScript(formulaScript)
							  .formulaWorkspace(formulaWorkspace)
							  .description(description)
							  .isActive(isActive)
							  .isUseNotify(isUseNotify)
							  .build();
	}

}
