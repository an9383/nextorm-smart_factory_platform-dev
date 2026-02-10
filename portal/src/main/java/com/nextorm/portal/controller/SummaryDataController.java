package com.nextorm.portal.controller;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.portal.dto.summary.*;
import com.nextorm.portal.service.SummaryDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryDataController {
	private final SummaryDataService servie;

	@GetMapping("/trend")
	public SummaryTrendResponseDto getTrendData(SummaryTrendRequestDto request) {
		return servie.getTrendData(request);
	}

	@GetMapping("/fast-trend")
	public FastTrendResponseDto getFastTrendData(FastTrendRequestDto request) {
		return servie.getFastTrendData(request);
	}

	@GetMapping("/monthly-data-report")
	public List<SummaryMonthlyDataResponseDto> getMonthlySummaryData(
		@RequestParam(name = "parameterId") Long parameterId,
		@RequestParam(name = "toYear") Long toYear,
		@RequestParam(name = "fromYear") Long fromYear
	) {
		return servie.getMonthlySummaryData(parameterId, toYear, fromYear);
	}

	@GetMapping("/parameter-report")
	public List<SummaryDataReportResponseDto> getSumBaseAtSummaryDataByPeriodType(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate,
		@RequestParam(name = "periodType") SummaryPeriodType periodType
	) {
		return servie.getSummaryReportData(parameterIds, fromDate, toDate, periodType);
	}

	@GetMapping("/parameter-report-pdf")
	public byte[] getParameterReportPdf(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate,
		@RequestParam(name = "periodType") SummaryPeriodType periodType,
		@RequestParam(name = "note") String note
	) {
		return servie.getParameterReportPdf(parameterIds, fromDate, toDate, periodType, note);
	}
}
