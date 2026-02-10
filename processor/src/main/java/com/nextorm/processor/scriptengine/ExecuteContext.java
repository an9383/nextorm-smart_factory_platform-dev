package com.nextorm.processor.scriptengine;

import java.util.Map;

public interface ExecuteContext {
	Long getExecutionParameterId();

	Map<Long, Object> getParamIdValueMap();

	Map<Long, Object> getPreviousParamIdValueMap();
}
