package com.nextorm.portal.dto.collectorconfig;

import com.nextorm.common.db.entity.CollectorConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CollectorConfigCreateRequestDto {
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;
	private List<Long> toolIds = new ArrayList<>();

	public CollectorConfig toEntity() {
		return CollectorConfig.builder()
							  .name(name)
							  .isUseFailover(isUseFailover)
							  .systemIp(systemIp)
							  .connectionTimeout(connectionTimeout)
							  .hosts(hosts)
							  .build();
	}
}
