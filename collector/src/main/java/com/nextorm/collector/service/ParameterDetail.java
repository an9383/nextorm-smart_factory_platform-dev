package com.nextorm.collector.service;

import com.nextorm.common.db.entity.Parameter;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ParameterDetail {
	private Long id;
	private Long toolId;
	private String name;
	private Parameter.DataType dataType;
	private Map<String, Object> extraData;
}
