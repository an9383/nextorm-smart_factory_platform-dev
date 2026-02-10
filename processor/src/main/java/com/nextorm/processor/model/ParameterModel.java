package com.nextorm.processor.model;

import com.nextorm.common.db.entity.Parameter;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class ParameterModel {
	private final Long id;
	private final String name;
	private final Parameter.DataType dataType;
	private final Integer order;
	private final boolean isVirtual;
	private final String virtualScript;
	private final List<Long> calculationRequiredVirtualParameterIds;
	private Double target;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;
	private boolean isSpecAvailable;
	private boolean isManualSpecCalculationTarget;
	private boolean isAutoSpecCalculationTarget;
	private String type;

	public static ParameterModel of(Parameter parameter) {
		List<Long> calculationRequiredParameterIds = List.of();
		if (parameter.isVirtual()) {
			calculationRequiredParameterIds = parameter.getCalculationRequiredParameters()
													   .stream()
													   .filter(Parameter::isVirtual)
													   .map(Parameter::getId)
													   .toList();
		}

		return ParameterModel.builder()
							 .id(parameter.getId())
							 .name(parameter.getName())
							 .dataType(parameter.getDataType())
							 .order(parameter.getOrder())

							 .target(parameter.getTarget())
							 .ucl(parameter.getUcl())
							 .lcl(parameter.getLcl())
							 .usl(parameter.getUsl())
							 .lsl(parameter.getLsl())

							 .isVirtual(parameter.isVirtual())
							 .virtualScript(parameter.getVirtualScript())
							 .calculationRequiredVirtualParameterIds(calculationRequiredParameterIds)

							 .isSpecAvailable(parameter.isSpecAvailable())
							 .isManualSpecCalculationTarget(parameter.isManualSpecCalculationTarget())
							 .isAutoSpecCalculationTarget(parameter.isAutoSpecCalculationTarget())
							 .type(String.valueOf(parameter.getType()))
							 .build();
	}

	public void disableAutoSpecCalculationTarget() {
		this.isAutoSpecCalculationTarget = false;
	}

	public Parameter toEntity() {
		return Parameter.builder()
						.id(id)
						.name(name)
						.dataType(dataType)

						.target(target)
						.ucl(ucl)
						.lcl(lcl)
						.usl(usl)
						.lsl(lsl)

						.isVirtual(isVirtual)
						.virtualScript(virtualScript)

						.isSpecAvailable(isSpecAvailable)

						.build();
	}
}
