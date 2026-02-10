package com.nextorm.portal.dto.widget;

import com.nextorm.portal.dto.weather.enums.PrecipitationFoam;
import com.nextorm.portal.dto.weather.enums.SkyCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolStatusWidgetResponseDto {
	private Integer batteryRemaining;
	private Integer goodCnt;
	private Integer badCnt;
	private List<String> badParameter;
	private List<String> goodParameter;
	private Double latitude;
	private Double longitude;
	private String temperature;
	private SkyCondition skyCondition;
	private PrecipitationFoam precipitationFoam;
	private Double avgVelocity;
	private Double maxVelocity;
	private Double minVelocity;
	private Double distance;
	private String markers;
}
