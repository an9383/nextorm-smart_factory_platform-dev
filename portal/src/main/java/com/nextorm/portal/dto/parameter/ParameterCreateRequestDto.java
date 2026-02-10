package com.nextorm.portal.dto.parameter;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterCreateRequestDto {
	private Long toolId;
	private Long dcpConfigId;
	private String name;
	private Parameter.Type type;
	private Parameter.DataType dataType;
	private Boolean isSpecAvailable;
	private Boolean isAutoSpec;
	private Boolean isReqRecalculate;
	private Integer autoCalcCurrCnt;
	private Integer autoCalcPeriod;
	private Integer order;
	private String toolName;
	private String unit;
	private Double target;
	private Double lsl;
	private Double lcl;
	private Double ucl;
	private Double usl;
	private String metaValue;

	public Parameter toEntity(Tool tool) {
		return Parameter.builder()
						.tool(tool)
						.name(name)
						.type(type)
						.dataType(dataType)
						.isSpecAvailable(isSpecAvailable)
						.isAutoSpec(isAutoSpec)
						.isReqRecalculate(isReqRecalculate)
						.autoCalcCurrCnt(autoCalcCurrCnt)
						.autoCalcPeriod(autoCalcPeriod)
						.unit(unit)
						.target(target)
						.ucl(ucl)
						.lcl(lcl)
						.usl(usl)
						.lsl(lsl)
						.metaValue(metaValue)
						.order(order)
						.build();
	}
}
