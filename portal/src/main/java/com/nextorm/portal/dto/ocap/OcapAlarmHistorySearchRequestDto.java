package com.nextorm.portal.dto.ocap;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OcapAlarmHistorySearchRequestDto {
	private LocalDateTime fromDate;
	private LocalDateTime toDate;
}
