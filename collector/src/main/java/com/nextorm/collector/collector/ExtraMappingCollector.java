package com.nextorm.collector.collector;

import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExtraMappingCollector implements Collector {
	private final DataCollectPlan dataCollectPlan;

	@Override
	public void run() {
	}

	@Override
	public DataCollectPlan getConfig() {
		return dataCollectPlan;
	}
}
