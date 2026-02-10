package com.nextorm.portal.dto.parameterdata;

import com.nextorm.common.db.entity.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDataSpecOutCountDto {

	private Long parameterId;
	private String parameterName;
	private Parameter.Type parameterType;
	private Parameter.DataType parameterDataType;
	private Long toolId;
	private String toolName;

	private long specOutCnt;
}
