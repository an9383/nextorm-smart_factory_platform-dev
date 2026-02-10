package com.nextorm.portal.dto.collectorconfig;

import com.nextorm.common.db.entity.CollectorConfig;
import com.nextorm.common.db.entity.SummaryConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectorConfigUpdateRequestDto {
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;
	private List<Long> toolIds = new ArrayList<>();

	public CollectorConfig toEntity(
	) {
		return CollectorConfig.builder()
							  .name(name)
							  .isUseFailover(isUseFailover)
							  .systemIp(systemIp)
							  .connectionTimeout(connectionTimeout)
							  .hosts(hosts)
							  .build();
	}
}
