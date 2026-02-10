package com.nextorm.portal.dto.summaryconfig;

import com.nextorm.common.db.entity.SummaryConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryConfigFailoverResponseDto {
	private Long id;
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;

	public static SummaryConfigFailoverResponseDto from(
		SummaryConfig summaryConfig
	) {
		return SummaryConfigFailoverResponseDto.builder()
											   .id(summaryConfig.getId())
											   .name(summaryConfig.getName())
											   .isUseFailover(summaryConfig.getIsUseFailover())
											   .systemIp(summaryConfig.getSystemIp())
											   .connectionTimeout(summaryConfig.getConnectionTimeout())
											   .hosts(summaryConfig.getHosts())
											   .build();
	}

}
