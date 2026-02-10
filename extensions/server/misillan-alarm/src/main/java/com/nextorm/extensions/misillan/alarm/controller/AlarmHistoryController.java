package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.AlarmHistoryDto;
import com.nextorm.extensions.misillan.alarm.service.AlarmHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/misillan-alarm/alarms")
@RequiredArgsConstructor
public class AlarmHistoryController {

	private final AlarmHistoryService alarmManagementService;

	@GetMapping("")
	public ResponseEntity<List<AlarmHistoryDto>> getAlarmList(
		@RequestParam(required = false) String startDt,
		@RequestParam(required = false) String endDt
	) {
		return ResponseEntity.ok(alarmManagementService.getAlarmList(startDt, endDt));
	}

	@GetMapping("/unread/{userId}")
	public ResponseEntity<List<AlarmHistoryDto>> getUnreadAlarmList(@PathVariable(name = "userId") Long userId) {
		return ResponseEntity.ok(alarmManagementService.getUnreadAlarmList(userId));
	}

	@GetMapping("/alarm/{alarmId}/read/{userId}")
	public ResponseEntity<AlarmHistoryDto> markAlarmAsRead(
		@PathVariable Long alarmId,
		@PathVariable Long userId
	) {
		return ResponseEntity.ok()
							 .body(alarmManagementService.markAlarmAsRead(alarmId, userId));
	}

	@PostMapping("/read/all/{userId}")
	public ResponseEntity<Void> readAllAlarm(@PathVariable Long userId) {
		alarmManagementService.readAllAlarm(userId);
		return ResponseEntity.noContent()
							 .build();
	}

}
