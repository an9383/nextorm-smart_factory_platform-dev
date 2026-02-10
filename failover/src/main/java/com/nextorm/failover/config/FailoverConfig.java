package com.nextorm.failover.config;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
public class FailoverConfig {
	public enum systemType {
		processor, summarizer, collector
	}

	private String failoverClassInfo;
	private String systemIp;
	private String systemType;
	private String processName;

	private boolean isUse;
	private int zookeeperConnectionTimeout;
	private String zookeeperHosts;
}
