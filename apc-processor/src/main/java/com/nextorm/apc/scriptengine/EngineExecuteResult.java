package com.nextorm.apc.scriptengine;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class EngineExecuteResult {
	private boolean success;
	private Map<String, Object> value;

	public static EngineExecuteResult success(Map<String, Object> value) {
		EngineExecuteResult result = new EngineExecuteResult();
		result.success = true;
		result.value = value;
		return result;
	}

	public static EngineExecuteResult error() {
		EngineExecuteResult result = new EngineExecuteResult();
		result.success = false;
		return result;
	}
}
