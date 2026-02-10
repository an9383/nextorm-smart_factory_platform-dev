package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;

public class GetSelfRecentlyValue implements VpCalculateExecutor {
	@Override
	public String getScript() {
		return """
			function getSelfRecentlyValue() {
				const findValue = %s[%s];
				if (findValue === undefined) {
					return null;
				}
				return findValue;
			}
			""".formatted(ScriptEngine.CONTEXT_CONTAINER_NAME, ScriptEngine.EXECUTION_PARAMETER_ID_NAME);
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getSelfRecentlyValue", this);
	}
}
