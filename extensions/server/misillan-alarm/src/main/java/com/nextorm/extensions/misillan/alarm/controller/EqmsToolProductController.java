package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.eqms.EqmsProductResponseDto;
import com.nextorm.extensions.misillan.alarm.dto.eqms.EqmsToolResponseDto;
import com.nextorm.extensions.misillan.alarm.service.EqmsToolProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/misillan-alarm/eqms")
@RequiredArgsConstructor
public class EqmsToolProductController {
	private final EqmsToolProductService eqmsToolProductService;

	@GetMapping("/tools")
	public List<EqmsToolResponseDto> getToolList() {
		return eqmsToolProductService.getToolList();
	}

	@GetMapping("/products")
	public List<EqmsProductResponseDto> getProductList() {
		return eqmsToolProductService.getParameterList();
	}
}
