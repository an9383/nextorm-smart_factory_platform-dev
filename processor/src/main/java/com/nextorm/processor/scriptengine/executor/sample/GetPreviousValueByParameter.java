package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;

public class GetPreviousValueByParameter implements VpCalculateExecutor {
	@Override
	public String getScript() {
		return """
			function getPreviousValueByParameter(parameterId) {
				const findValue = %s[parameterId];
				if (findValue === undefined) {
					return null;
				}
				return findValue;
			}
			""".formatted(ScriptEngine.PREVIOUS_CONTEXT_CONTAINER_NAME);
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getPreviousValue", this);
	}
}
