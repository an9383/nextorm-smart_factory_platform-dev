package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;

public class GetRandomValue implements VpCalculateExecutor {
	@Override
	public String getScript() {
		return """
			function getRandomValue() {
			    return self_generator.getRandomValue();
			}
			""";
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_generator", this);
	}

	public int getRandomValue() {
		return (int)(Math.random() * 100);
	}
}
