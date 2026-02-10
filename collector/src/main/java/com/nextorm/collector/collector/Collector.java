package com.nextorm.collector.collector;

import com.nextorm.common.define.collector.DataCollectPlan;

public interface Collector extends Runnable {
	default void shutdown() {
	}

	DataCollectPlan getConfig();
}
