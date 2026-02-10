package com.nextorm.portal.dto;

import com.nextorm.common.db.entity.FaultHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class FaultHistoryResponseDto {
	private Long id;
	private Long parameterId;
	private String paramValue;
	private boolean isSpecLimitOver;
	private boolean isCtrlLimitOver;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;
	private boolean isLslOver;
	private boolean isLclOver;
	private boolean isUclOver;
	private boolean isUslOver;
	private LocalDateTime faultAt;

	public static FaultHistoryResponseDto of(FaultHistory faultHistory) {
		return FaultHistoryResponseDto.builder()
									  .id(faultHistory.getId())
									  .parameterId(faultHistory.getParameterId())
									  .paramValue(faultHistory.getParamValue())
									  .isSpecLimitOver(faultHistory.isSpecLimitOver())
									  .isCtrlLimitOver(faultHistory.isCtrlLimitOver())
									  .ucl(faultHistory.getUcl())
									  .lcl(faultHistory.getLcl())
									  .usl(faultHistory.getUsl())
									  .lsl(faultHistory.getLsl())
									  .isLslOver(faultHistory.isLslOver())
									  .isLclOver(faultHistory.isLclOver())
									  .isUclOver(faultHistory.isUclOver())
									  .isUslOver(faultHistory.isUslOver())
									  .faultAt(faultHistory.getFaultAt())
									  .build();
	}
}
