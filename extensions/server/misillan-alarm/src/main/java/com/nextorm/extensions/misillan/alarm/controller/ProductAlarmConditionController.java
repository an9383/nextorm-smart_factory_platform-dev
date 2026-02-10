package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionCreateDto;
import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionModifyDto;
import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionResponseDto;
import com.nextorm.extensions.misillan.alarm.service.ProductAlarmConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/misillan-alarm/product-alarm-condition")
@RequiredArgsConstructor
public class ProductAlarmConditionController {

	private final ProductAlarmConditionService productAlarmConditionService;

	@GetMapping("")
	public List<ProductAlarmConditionResponseDto> getAll(){
		return productAlarmConditionService.getAll();
	}

	@PostMapping("")
	public void createAlarmCondition(@RequestBody ProductAlarmConditionCreateDto conditionCreateDto) {
		productAlarmConditionService.createCondition(conditionCreateDto);
	}

	@PutMapping("/{id}")
	public void modifyAlarmCondition(
		@PathVariable Long id,
		@RequestBody ProductAlarmConditionModifyDto conditionModifyDto
	) {
		productAlarmConditionService.modifyCondition(id, conditionModifyDto);
	}

	@DeleteMapping("/{id}")
	public void deleteAlarmCondition(@PathVariable Long id) {
		productAlarmConditionService.deleteCondition(id);
	}
}
