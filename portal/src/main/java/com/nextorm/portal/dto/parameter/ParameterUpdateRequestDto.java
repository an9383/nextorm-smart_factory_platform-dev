package com.nextorm.portal.dto.parameter;

import com.nextorm.common.db.entity.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterUpdateRequestDto {
	private Long dcpConfigId;
	private String name;
	private Parameter.Type type;
	private Parameter.DataType dataType;
	private String unit;
	private Boolean isSpecAvailable;
	private Boolean isAutoSpec;
	private Boolean isReqRecalculate;
	private Integer autoCalcCurrCnt;
	private Integer autoCalcPeriod;
	private Integer order;
	private Double target;
	private Double lsl;
	private Double lcl;
	private Double ucl;
	private Double usl;
	private String metaValue;

	public Parameter toEntity() {
		return Parameter.builder()
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
