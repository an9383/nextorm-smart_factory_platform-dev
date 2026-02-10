package com.nextorm.processor.worker.datacollector;

import java.util.List;

public interface DataCollector {
	List<CollectData> collectData();

	void commitOffset();

	void close();
}
