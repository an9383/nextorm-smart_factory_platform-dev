package com.nextorm.portal.controller;

import com.nextorm.portal.dto.parameterdata.*;
import com.nextorm.portal.enums.Operator;
import com.nextorm.portal.enums.TimeCriteria;
import com.nextorm.portal.service.ParameterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/parameter-data")
@RequiredArgsConstructor
public class ParameterDataController {
	private final ParameterDataService parameterDataService;

	@GetMapping(path = "/trend")
	public ResponseEntity<ParameterDataTrendDto> getParameterDataTrend(
		@RequestParam(name = "parameterId") Long parameterId,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		ParameterDataTrendDto result = parameterDataService.getParameterDataTrend(parameterId, from, to);
		return ResponseEntity.ok(result);
	}

	@GetMapping(path = "/image")
	public ParameterDataDto getImageParameterDataByToolAndTraceAt(
		@RequestParam(name = "toolId") Long toolId,
		@RequestParam(name = "dateTime") LocalDateTime dateTime
	) {
		return parameterDataService.getImageParameterDataByToolAndTraceAt(toolId, dateTime);
	}

	@GetMapping(path = "/trend/multi")
	public ResponseEntity<List<ParameterDataTrendDto>> getMultiParameterDataTrend(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		List<ParameterDataTrendDto> result = parameterDataService.getParameterDataTrend(parameterIds, from, to);
		return ResponseEntity.ok(result);
	}

	@GetMapping(path = "/distribution")
	public ResponseEntity<List<ParameterDataDistributionDto>> getParameterDataNormalDistribution(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		List<ParameterDataDistributionDto> result = parameterDataService.getParameterDataNormalDistribution(parameterIds,
			from,
			to);
		return ResponseEntity.ok(result);
	}

	@GetMapping(path = "/spec-out/count")
	public ResponseEntity<List<ParameterDataSpecOutCountDto>> getParameterDataSpecOutCount(
		@RequestParam(name = "parameterIds") List<Long> parameterIds,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		List<ParameterDataSpecOutCountDto> result = parameterDataService.getParameterDataSpecOutCount(parameterIds,
			from,
			to);
		return ResponseEntity.ok(result);
	}

	@GetMapping(path = "/eco-parameter-pivot")
	public ResponseEntity<ReplyDataDto> getEcoParameters(
		@RequestParam(name = "parameterId") Long parameterId,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		parameterId = Long.valueOf(String.valueOf(parameterId));
		ReplyDataDto result = parameterDataService.getParameterDataPivot(parameterId, fromDate, toDate);
		return ResponseEntity.ok(result);
	}

	/**
	 * 파라미터의 집계 단위별(TimeCriteria) 집계 형태(Operation) 데이터를 조회
	 */
	@GetMapping("/statistics/{parameterId}")
	public ParameterStatisticsResponseDto getStatisticsByParameter(
		@PathVariable(name = "parameterId") Long parameterId,
		@RequestParam(name = "operator") Operator operation,
		@RequestParam(name = "timeCriteria") TimeCriteria timeCriteria,
		@RequestParam(name = "from", required = false) LocalDateTime from,
		@RequestParam(name = "to", required = false) LocalDateTime to
	) {
		return parameterDataService.getStatisticsByParameterId(parameterId, operation, timeCriteria, from, to);
	}

	@GetMapping("/recent")
	public List<RecentParameterDataResponseDto> getRecentParameterData(RecentParameterDataSearchRequestDto searchRequestDto) {
		return parameterDataService.getRecentParameterData(searchRequestDto.getParameterId(),
			searchRequestDto.getLastDataId(),
			searchRequestDto.getLimit());
	}

	@GetMapping(path = "/underwater-terrain")
	public List<UnderWaterTerrainDto> getUnderWaterTerrainData(
		@RequestParam(name = "toolId") Long toolId,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		List<UnderWaterTerrainDto> underWaterTerrainDtoList = parameterDataService.getUnderWaterTerrainData(toolId,
			fromDate,
			toDate);
		return underWaterTerrainDtoList;
	}

	/**
	 * String 타입의 파라미터의 데이터를 중복 제거하여 조회
	 */
	@GetMapping(path = "/distinct")
	public List<String> getDistinctParameterData(
		@RequestParam(name = "parameterId") Long parameterId,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		return parameterDataService.getDistinctParameterDataByStringTypeParameter(parameterId, from, to);
	}

	@GetMapping("/recipe-trend")
	public List<RecipeTrendDto> getRecipeTrend(RecipeTrendRequestDto requestDto) {
		return parameterDataService.getRecipeTrend(requestDto);
	}

	@GetMapping("/recipe-dtw-trend")
	public List<RecipeDtwTrendDto> getRecipeDtwTrend(RecipeTrendRequestDto requestDto) {
		return parameterDataService.getRecipeDtwTrend(requestDto);
	}

	@GetMapping("/{parameterId}/pagination")
	public ResponseEntity<Page<ParameterRawDataDto>> getParameterDatasByPageable(
		@PathVariable(name = "parameterId") Long parameterId,
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to,
		Pageable pageable
	) {
		return ResponseEntity.ok(parameterDataService.getParameterDatasByPageable(parameterId, from, to, pageable));
	}

	@GetMapping("/latest-within-period")
	public List<ParameterDataWithinPeriodDto> getLatestParameterDataWithinPeriod(
		@RequestParam List<Long> parameterIds,
		@RequestParam Integer period
	) {
		return parameterDataService.getLatestParameterDataWithinPeriod(parameterIds, period);
	}

}
