package com.nextorm.portal.dto.collectorconfig;

import com.nextorm.common.db.entity.CollectorConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorConfigFailoverResponseDto {
	private Long id;
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;

	public static CollectorConfigFailoverResponseDto from(
		CollectorConfig collectorConfig
	) {
		return CollectorConfigFailoverResponseDto.builder()
												 .id(collectorConfig.getId())
												 .name(collectorConfig.getName())
												 .isUseFailover(collectorConfig.isUseFailover())
												 .systemIp(collectorConfig.getSystemIp())
												 .connectionTimeout(collectorConfig.getConnectionTimeout())
												 .hosts(collectorConfig.getHosts())
												 .build();
	}

}
