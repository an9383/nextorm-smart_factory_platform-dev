package com.nextorm.portal.dto.processconfig;

import com.nextorm.common.db.entity.ProcessConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessConfigFailoverResponseDto {
	private Long id;
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;

	public static ProcessConfigFailoverResponseDto from(
		ProcessConfig processConfig
	) {
		return ProcessConfigFailoverResponseDto.builder()
											   .id(processConfig.getId())
											   .name(processConfig.getName())
											   .isUseFailover(processConfig.getIsUseFailover())
											   .systemIp(processConfig.getSystemIp())
											   .connectionTimeout(processConfig.getConnectionTimeout())
											   .hosts(processConfig.getHosts())
											   .build();
	}

}
