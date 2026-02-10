package com.nextorm.portal.dto.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class VirtualParameterCreateRequestDto {
	private Long toolId;
	private Long dcpConfigId;
	private String name;
	private Parameter.Type type;
	private Parameter.DataType dataType;
	private Integer order;

	private Double target;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;

	@JsonProperty("isSpecAvailable")
	private boolean isSpecAvailable;
	@JsonProperty("isAutoSpec")
	private boolean isAutoSpec;
	private Integer autoCalcPeriod;

	@JsonProperty("isReqRecalculate")
	private boolean isReqRecalculate;

	private String virtualScript;
	private String virtualWorkspace;

	private List<MappingParameterDto> mappingParameters = new ArrayList<>();

	public Parameter toEntity(Tool tool) {
		return Parameter.builder()
						.tool(tool)
						.name(name)
						.type(type)
						.dataType(dataType)
						.target(target)
						.ucl(ucl)
						.lcl(lcl)
						.usl(usl)
						.lsl(lsl)
						.order(order)
						.isVirtual(true)
						.isSpecAvailable(isSpecAvailable)
						.isAutoSpec(isAutoSpec)
						.autoCalcPeriod(autoCalcPeriod)
						.isReqRecalculate(isReqRecalculate)
						.virtualScript(virtualScript)
						.virtualWorkspace(virtualWorkspace)
						.build();
	}
}
