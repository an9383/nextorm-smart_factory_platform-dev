package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.AlarmConditionRequestDto;
import com.nextorm.extensions.misillan.alarm.dto.AlarmConditionResponseDto;
import com.nextorm.extensions.misillan.alarm.service.AlarmConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/misillan-alarm/settings")
@RequiredArgsConstructor
public class AlarmConditionController {
	private final AlarmConditionService alarmConditionService;

	@GetMapping(value = "")
	public ResponseEntity<List<AlarmConditionResponseDto>> getAlarmConditions() {
		return ResponseEntity.ok(alarmConditionService.getAlarmConditions());
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<AlarmConditionResponseDto> getAlarmConditionById(@PathVariable(name = "id") Long id) {
		return ResponseEntity.ok(alarmConditionService.getAlarmConditionById(id));
	}

	@PostMapping(value = "")
	public ResponseEntity<AlarmConditionResponseDto> createAlarmCondition(@RequestBody AlarmConditionRequestDto alarmConditionRequest) {
		return ResponseEntity.ok(alarmConditionService.createAlarmCondition(alarmConditionRequest));
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<AlarmConditionResponseDto> modifyAlarmCondition(
		@PathVariable(name = "id") Long id,
		@RequestBody AlarmConditionRequestDto alarmConditionRequest
	) {
		return ResponseEntity.ok(alarmConditionService.modifyAlarmCondition(id, alarmConditionRequest));
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteAlarmCondition(@PathVariable(name = "id") Long id) {
		alarmConditionService.deleteAlarmCondition(id);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/bulk-delete")
	public ResponseEntity<String> bulkDeleteAlarmCondition(@RequestBody List<Long> ids) {
		alarmConditionService.bulkDeleteAlarmCondition(ids);
		return ResponseEntity.ok("ok");
	}

	@PutMapping(value = "/{id}/active")
	public ResponseEntity<AlarmConditionResponseDto> updateAlarmConditionActive(
		@PathVariable(name = "id") Long id,
		@RequestParam boolean isActive
	) {
		return ResponseEntity.ok(alarmConditionService.updateAlarmConditionActive(id, isActive));
	}

}
