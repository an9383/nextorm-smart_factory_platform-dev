package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetValueByParameter implements VpCalculateExecutor {

	@Override
	public String getScript() {
		return """
			function getValueByParameter(parameterId) {
				const container = %s;
				//return container[parameterId];
				return self_getValueByParameter.loggingWithReturnValue(parameterId, container[parameterId]);
			}
			""".formatted(ScriptEngine.CONTEXT_CONTAINER_NAME);
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getValueByParameter", this);
	}

	public Double loggingWithReturnValue(
		String parameterId,
		Object value
	) {
		log.info("findValue id:{}, value: {}", parameterId, value);
		if (value == null) {
			return null;
		}
		return Double.parseDouble(value.toString());
	}
}
