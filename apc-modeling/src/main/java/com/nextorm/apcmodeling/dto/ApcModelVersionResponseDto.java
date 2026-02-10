package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.apc.entity.ApcModelVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApcModelVersionResponseDto {
	private Long apcModelId;
	private Long id;
	private Integer version;
	private String modelName;
	private String formulaScript;
	private String formulaWorkspace;
	private String description;
	private String conditions;
	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isActive")
	private boolean isActive;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;

	public static ApcModelVersionResponseDto from(
		ApcModelVersion apcModelVersion
	) {
		return ApcModelVersionResponseDto.builder()
										 .apcModelId(apcModelVersion.getApcModel()
																	.getId())
										 .id(apcModelVersion.getId())
										 .modelName(apcModelVersion.getApcModel()
																   .getModelName())
										 .version(apcModelVersion.getVersion())
										 .formulaScript(apcModelVersion.getFormulaScript())
										 .formulaWorkspace(apcModelVersion.getFormulaWorkspace())
										 .description(apcModelVersion.getDescription())
										 .conditions(apcModelVersion.getApcModel()
																	.getCondition())
										 .isUse(apcModelVersion.getApcModel()
															   .isUse())
										 .isActive(apcModelVersion.isActive())
										 .isUseNotify(apcModelVersion.isUseNotify())
										 .build();
	}

}
