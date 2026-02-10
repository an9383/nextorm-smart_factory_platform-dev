package com.nextorm.portal.dto.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Parameter;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class VirtualParameterResponseDto {
	private Long id;
	private Long dcpConfigId;
	private Long toolId;
	private String name;
	private Parameter.Type type;
	private Parameter.DataType dataType;
	private Integer order;

	private Double target;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;

	private Boolean isSpecAvailable;
	private Boolean isAutoSpec;
	private Integer autoCalcPeriod;

	private Boolean isReqRecalculate;

	@JsonProperty("isVirtual")
	private boolean isVirtual;
	private String virtualScript;
	private String virtualWorkspace;

	@Builder.Default
	private List<Long> mappingParameterIds = new ArrayList<>();

	public static VirtualParameterResponseDto of(
		Parameter virtualParameter,
		DcpConfig dcpConfig,
		List<Long> mappingParameterIds
	) {
		return VirtualParameterResponseDto.builder()
										  .id(virtualParameter.getId())
										  .dcpConfigId(dcpConfig.getId())
										  .toolId(virtualParameter.getTool()
																  .getId())
										  .name(virtualParameter.getName())
										  .type(virtualParameter.getType())
										  .dataType(virtualParameter.getDataType())
										  .order(virtualParameter.getOrder())
										  .target(virtualParameter.getTarget())
										  .ucl(virtualParameter.getUcl())
										  .lcl(virtualParameter.getLcl())
										  .usl(virtualParameter.getUsl())
										  .lsl(virtualParameter.getLsl())
										  .isVirtual(virtualParameter.isVirtual())
										  .isSpecAvailable(virtualParameter.isSpecAvailable())
										  .isAutoSpec(virtualParameter.isAutoSpec())
										  .autoCalcPeriod(virtualParameter.getAutoCalcPeriod())
										  .isReqRecalculate(virtualParameter.isReqRecalculate())
										  .virtualScript(virtualParameter.getVirtualScript())
										  .virtualWorkspace(virtualParameter.getVirtualWorkspace())
										  .mappingParameterIds(mappingParameterIds)
										  .build();
	}
}
