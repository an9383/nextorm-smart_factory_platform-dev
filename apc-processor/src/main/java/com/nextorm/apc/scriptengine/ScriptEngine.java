package com.nextorm.apc.scriptengine;

import java.util.Map;

public interface ScriptEngine {
	EngineExecuteResult execute(ExecuteArguments arguments);

	record ExecuteArguments(Map<String, Object> source, Map<String, Object> metaData) {
		public ExecuteArguments {
			if (source == null) {
				source = Map.of();
			}
			if (metaData == null) {
				metaData = Map.of();
			}
		}
	}
}
