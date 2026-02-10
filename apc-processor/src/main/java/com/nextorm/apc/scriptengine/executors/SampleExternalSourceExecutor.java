package com.nextorm.apc.scriptengine.executors;

import com.nextorm.apc.scriptengine.BindingMember;
import com.nextorm.apc.scriptengine.ExternalSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SampleExternalSourceExecutor implements VpCalculateExecutor {
	private final ExternalSourceRepository externalSourceRepository;

	@Override
	public String getScript() {
		return """
			function externalSourceSample() {
				return self_externalSourceSample.execute();
			}
			""";
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("sample", this);
	}

	public void execute() {
		log.info("externalSourceSample: {}", externalSourceRepository.sample());
	}
}
