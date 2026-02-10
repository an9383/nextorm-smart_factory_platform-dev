package com.nextorm.portal.dto.ocap;

import com.nextorm.common.db.entity.OcapAlarm;
import com.nextorm.common.db.entity.OcapAlarmHistory;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class OcapAlarmHistoryResponseDto {
	private String name;
	private Long toolId;
	private String toolName;
	private Long parameterId;
	private String parameterName;
	private LocalDateTime faultAt;
	private String alarmCondition;

	public static OcapAlarmHistoryResponseDto of(OcapAlarmHistory ocapAlarmHistory) {
		OcapAlarm ocapAlarm = ocapAlarmHistory.getOcapAlarm();
		Tool tool = ocapAlarm.getTool();
		Parameter parameter = ocapAlarm.getParameter();

		return OcapAlarmHistoryResponseDto.builder()
										  .name(ocapAlarm.getName())
										  .toolId(tool.getId())
										  .toolName(tool.getName())
										  .parameterId(parameter.getId())
										  .parameterName(parameter.getName())
										  .faultAt(ocapAlarmHistory.getFaultAt())
										  .alarmCondition(ocapAlarmHistory.getAlarmCondition()
																		  .getName())
										  .build();
	}
}
