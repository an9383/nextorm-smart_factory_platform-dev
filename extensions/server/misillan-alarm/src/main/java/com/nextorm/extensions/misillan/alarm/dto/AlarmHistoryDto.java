package com.nextorm.extensions.misillan.alarm.dto;

import com.nextorm.extensions.misillan.alarm.entity.AlarmHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmHistoryDto {
	private Long id;
	private String toolName;
	private String productName;
	private Double value;
	private LocalDateTime alarmDt;
	private Double threshold;
	private String conditionType;
	private String parameterName;
	private String triggerType;

	public static AlarmHistoryDto from(
		AlarmHistory alarmHistory

	) {

		return AlarmHistoryDto.builder()
							  .id(alarmHistory.getId())
							  .toolName(alarmHistory.getToolName())
							  .productName(alarmHistory.getProductName())
							  .value(alarmHistory.getValue())
							  .alarmDt(alarmHistory.getAlarmDt())
							  .threshold(alarmHistory.getThreshold())
							  .conditionType(alarmHistory.getConditionType() != null
											 ? alarmHistory.getConditionType()
														   .getDisplayName()
											 : null)
							  .triggerType(alarmHistory.getTriggerType() != null
										   ? alarmHistory.getTriggerType()
														 .getDisplayName()
										   : null)
							  .parameterName(alarmHistory.getParameterName())
							  .build();
	}
}