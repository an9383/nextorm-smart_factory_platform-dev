package com.nextorm.portal.dto.summaryconfig;

import com.nextorm.common.db.entity.SummaryConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SummaryConfigUpdateRequestDto {
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;
	private List<Long> toolIds = new ArrayList<>();

	public SummaryConfig toEntity(
	) {
		return SummaryConfig.builder()
							.name(name)
							.isUseFailover(isUseFailover)
							.systemIp(systemIp)
							.connectionTimeout(connectionTimeout)
							.hosts(hosts)
							.build();
	}
}
