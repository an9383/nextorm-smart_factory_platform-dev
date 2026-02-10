package com.nextorm.collector.configprovider;

import com.nextorm.common.define.collector.DataCollectPlan;

import java.util.List;

public interface ConfigProvider {
	List<DataCollectPlan> getConfig();

	List<DataCollectPlan> getConfigByToolId(Long toolId);
}
