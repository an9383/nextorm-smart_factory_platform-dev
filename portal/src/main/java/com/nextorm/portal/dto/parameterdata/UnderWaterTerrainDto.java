package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderWaterTerrainDto {
	private LocalDateTime traceAt;
	private Double latitude;
	private Double longitude;
	private Double depthData;
}
