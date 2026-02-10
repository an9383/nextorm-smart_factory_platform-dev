package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.model.ParameterModel;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetSpecValueByParameter implements VpCalculateExecutor {
	private final ParameterContainer parameterContainer;

	public GetSpecValueByParameter(ParameterContainer parameterContainer) {
		this.parameterContainer = parameterContainer;
	}

	@Override
	public String getScript() {
		return """
			function getSpecValueByParameter(parameterId, specType) {
				return self_getSpecValueByParameter.getSpecValue(parameterId, specType);
			}
			""";
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getSpecValueByParameter", this);
	}

	public Double getSpecValue(
		String parameterId,
		String specType
	) {
		ParameterModel parameter = parameterContainer.getParameterById(Long.parseLong(parameterId));
		if (parameter == null) {
			return null;
		}

		Double specValue = switch (specType.toLowerCase()) {
			case "ucl" -> parameter.getUcl();
			case "usl" -> parameter.getUsl();
			case "lsl" -> parameter.getLsl();
			case "lcl" -> parameter.getLcl();
			default -> null;
		};

		if (specValue == null) {
			log.debug("Spec value is null: {}", specType);
		}

		return specValue;
	}
}
