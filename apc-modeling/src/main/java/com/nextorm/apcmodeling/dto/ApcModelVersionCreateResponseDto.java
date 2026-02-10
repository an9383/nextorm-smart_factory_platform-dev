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
public class ApcModelVersionCreateResponseDto {
	private Long activeModelVersionId;
	private String formulaScript;
	private String formulaWorkspace;
	private String description;
	@JsonProperty("isUse")
	private boolean isUse;
	@JsonProperty("isUseNotify")
	private boolean isUseNotify;
	private String createBy;
	private LocalDateTime createAt;

	public static ApcModelVersionCreateResponseDto from(
		ApcModelVersion savedApcModelVersion,
		boolean isUse
	) {
		return ApcModelVersionCreateResponseDto.builder()
											   .activeModelVersionId(savedApcModelVersion.getId())
											   .formulaScript(savedApcModelVersion.getFormulaScript())
											   .formulaWorkspace(savedApcModelVersion.getFormulaWorkspace())
											   .description(savedApcModelVersion.getDescription())
											   .createBy(savedApcModelVersion.getCreateBy())
											   .createAt(savedApcModelVersion.getCreateAt())
											   .isUse(isUse)
											   .isUseNotify(savedApcModelVersion.isUseNotify())
											   .build();
	}
}
