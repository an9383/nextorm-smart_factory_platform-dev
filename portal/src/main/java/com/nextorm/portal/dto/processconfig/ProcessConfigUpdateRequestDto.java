package com.nextorm.portal.dto.processconfig;

import com.nextorm.common.db.entity.ProcessConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessConfigUpdateRequestDto {
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;
	private List<Long> toolIds = new ArrayList<>();

	public ProcessConfig toEntity(
	) {
		return ProcessConfig.builder()
							.name(name)
							.isUseFailover(isUseFailover)
							.systemIp(systemIp)
							.connectionTimeout(connectionTimeout)
							.hosts(hosts)
							.build();
	}
}
