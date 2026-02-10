package com.nextorm.portal.dto.parameter;

import com.nextorm.common.db.entity.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class ParameterSearchRequestDto {
	private List<Long> id;
	private Long toolId;
	private String name;
	private Boolean isVirtual;
	private Parameter.Type type;
	private List<Parameter.DataType> dataTypes;
}
