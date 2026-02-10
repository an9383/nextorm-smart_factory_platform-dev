package com.nextorm.portal.controller;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.portal.dto.aidas.ParameterDataAnalysisResponseDto;
import com.nextorm.portal.service.SummaryDataService;
import com.nextorm.portal.service.aidas.AIDasService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/aidas")
@RequiredArgsConstructor
public class AIDasController {

	private Logger logger = LoggerFactory.getLogger(AIDasController.class);

	private final AIDasService aiDasService;
	private final SummaryDataService summaryDataService;

	@GetMapping(path = "/parameter-analysis")
	public ParameterDataAnalysisResponseDto getParameterDataAnalysis(
		@RequestParam(name = "sessionId") String sessionId,
		@RequestParam(name = "parameterId") Long parameterId,
		@RequestParam(name = "startDt") LocalDateTime startDt,
		@RequestParam(name = "endDt") LocalDateTime endDt
	) {
		return aiDasService.getParameterDataAnalysis(sessionId, parameterId, startDt, endDt);
	}

	@GetMapping("/parameter-report-pdf")
	public ResponseEntity<byte[]> getParameterReportPdf(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate,
		@RequestParam(name = "periodType") SummaryPeriodType periodType
	) {

		byte[] pdfBytes = summaryDataService.getParameterReportPdf(parameterIds, fromDate, toDate, periodType, "");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);

		return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
	}
}
