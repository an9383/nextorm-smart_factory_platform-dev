package com.nextorm.portal.controller;

import com.nextorm.portal.dto.rule.RuleCreateRequestDto;
import com.nextorm.portal.dto.rule.RuleResponseDto;
import com.nextorm.portal.dto.rule.RuleUpdateRequestDto;
import com.nextorm.portal.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {
	private final RuleService ruleService;

	@GetMapping(path = "")
	public ResponseEntity<List<RuleResponseDto>> getRules() {
		List<RuleResponseDto> rules = ruleService.getRules();
		return ResponseEntity.ok(rules);
	}

	@PostMapping(path = "")
	public RuleResponseDto createRule(@RequestBody RuleCreateRequestDto ruleCreateRequestDto) {
		return ruleService.createRule(ruleCreateRequestDto);
	}

	@PutMapping(path = "/{ruleId}")
	public RuleResponseDto modifyRule(
		@PathVariable(name = "ruleId") Long ruleId,
		@RequestBody RuleUpdateRequestDto ruleUpdateRequestDto
	) {
		return ruleService.modifyRule(ruleId, ruleUpdateRequestDto);
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<String> deleteRule(@PathVariable(name = "id") Long id) throws Exception {
		ruleService.deleteRule(id);
		return ResponseEntity.ok("ok");
	}
}
