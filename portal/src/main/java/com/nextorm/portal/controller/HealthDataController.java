package com.nextorm.portal.controller;

import com.nextorm.portal.dto.parameterdata.HeatMapHealthDataResponseDto;
import com.nextorm.portal.dto.parameterdata.MonthlyHealthDataDto;
import com.nextorm.portal.dto.parameterdata.MonthlyHealthScoreByLocationResponseDto;
import com.nextorm.portal.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/health-data")
@RequiredArgsConstructor
public class HealthDataController {
	private final HealthDataService healthDataService;

	@GetMapping("/heatmap")
	public List<HeatMapHealthDataResponseDto> getHeatMapHealthData(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		return healthDataService.getHeatMapHealthData(parameterIds, fromDate, toDate);
	}

	@GetMapping(path = "/monthly-forecast")
	public List<MonthlyHealthDataDto> getMonthlyForecastHealthData(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		return healthDataService.getMonthlyForecastHealthData(parameterIds, fromDate, toDate);
	}

	@GetMapping(path = "/monthly-heatmap")
	public List<MonthlyHealthScoreByLocationResponseDto> getMonthlyHeatMapHealthData(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		return healthDataService.getMonthlyHeatMapHealthData(parameterIds, fromDate, toDate);
	}
}
