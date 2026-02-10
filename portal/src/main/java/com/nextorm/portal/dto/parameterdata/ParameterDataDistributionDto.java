package com.nextorm.portal.dto.parameterdata;

import com.nextorm.common.db.entity.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDataDistributionDto {

	private Long parameterId;
	private String parameterName;
	private Parameter.Type parameterType;
	private Parameter.DataType parameterDataType;
	private Long toolId;
	private String toolName;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Distribution {
		double value;
		double distributionValue;
	}

	List<Distribution> distributions = new ArrayList<>();
}
