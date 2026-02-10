package com.nextorm.processor.scriptengine;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ExecutionContext {
	private Long executionParameterId;
	private Map<Long, Object> paramIdValueMap;
	private Map<Long, Object> previousParamIdValueMap;
}
