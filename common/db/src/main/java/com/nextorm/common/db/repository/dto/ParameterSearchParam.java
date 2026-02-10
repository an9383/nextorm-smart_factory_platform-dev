package com.nextorm.common.db.repository.dto;

import com.nextorm.common.db.entity.Parameter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ParameterSearchParam {
	private List<Long> ids;
	private Long toolId;
	private String name;
	private Boolean isVirtual;
	private Parameter.Type type;
	private List<Parameter.DataType> dataTypes;
}
