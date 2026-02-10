package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.sfp.SfpParameterResponseDto;
import com.nextorm.extensions.misillan.alarm.service.SfpParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/misillan-alarm/sfp")
@RequiredArgsConstructor
public class SfpParameterController {

	private final SfpParameterService sfpParameterService;

	@GetMapping("/parameters")
	public List<SfpParameterResponseDto> getAll() {
		return sfpParameterService.getAll();
	}
}
