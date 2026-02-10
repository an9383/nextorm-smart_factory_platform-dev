package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.apc.entity.ApcModelVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApcModelCreateResponseDto {
	private Long activeModelVersionId;
	private Integer version;
	private String modelName;
	private String condition;

	private String formulaScript;
	private String formulaWorkspace;
	private String description;

	@JsonProperty("isUse")
	private boolean isUse;

	@JsonProperty("isUseNotify")
	private boolean isUseNotify;

	private Long apcModelId;
	private String createBy;
	private LocalDateTime createAt;

	public static ApcModelCreateResponseDto from(
		ApcModelVersion apcModelVersion
	) {
		return ApcModelCreateResponseDto.builder()
										.activeModelVersionId(apcModelVersion.getId())
										.modelName(apcModelVersion.getApcModel()
																  .getModelName())
										.version(apcModelVersion.getVersion())
										.condition(apcModelVersion.getApcModel()
																  .getCondition())
										.formulaScript(apcModelVersion.getFormulaScript())
										.formulaWorkspace(apcModelVersion.getFormulaWorkspace())
										.description(apcModelVersion.getDescription())
										.isUse(apcModelVersion.getApcModel()
															  .isUse())
										.apcModelId(apcModelVersion.getApcModel()
																   .getId())
										.createAt(apcModelVersion.getApcModel()
																 .getCreateAt())
										.createBy(apcModelVersion.getApcModel()
																 .getCreateBy())
										.isUseNotify(apcModelVersion.isUseNotify())
										.build();
	}

}
