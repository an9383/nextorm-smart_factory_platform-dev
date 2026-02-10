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
public class ApcModelUpdateRequestDto {
	private Long versionId;
	private String modelName;
	private String description;
	private String formulaScript;
	private String formulaWorkspace;
	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isActive")
	private boolean isActive;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;

	public ApcModelVersion toVersionEntity(
		ApcModel apcModel
	) {
		return ApcModelVersion.builder()
							  .apcModel(apcModel)
							  .formulaScript(formulaScript)
							  .formulaWorkspace(formulaWorkspace)
							  .id(versionId)
							  .description(description)
							  .isActive(isActive)
							  .isUseNotify(isUseNotify)
							  .build();
	}

}
