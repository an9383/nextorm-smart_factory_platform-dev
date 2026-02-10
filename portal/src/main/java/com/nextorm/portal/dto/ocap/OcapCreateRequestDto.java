package com.nextorm.portal.dto.ocap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OcapCreateRequestDto {
	private String name;
	@JsonProperty("isAlarmControlSpecOver")
	private boolean isAlarmControlSpecOver;
	@JsonProperty("isAlarmSpecOver")
	private boolean isAlarmSpecOver;
	private Long alarmIntervalCodeId;
	private Long toolId;
	private Long parameterId;
	private List<RecipientInfo> recipients = new ArrayList<>();    // 수신자
}
