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
public class ApcModelVersionListResponseDto {
	Long id;
	Integer version;
	String workspace;
	String description;
	@JsonProperty("isActive")
	boolean isActive;

	public static ApcModelVersionListResponseDto from(
		ApcModelVersion apcModelVersion
	) {
		return ApcModelVersionListResponseDto.builder()
											 .id(apcModelVersion.getId())
											 .version(apcModelVersion.getVersion())
											 .workspace(apcModelVersion.getFormulaWorkspace())
											 .description(apcModelVersion.getDescription())
											 .isActive(apcModelVersion.isActive())
											 .build();
	}
}
