package com.nextorm.apcmodeling.controller;

import com.nextorm.apcmodeling.dto.*;
import com.nextorm.apcmodeling.service.ApcSimulationDataService;
import com.nextorm.apcmodeling.service.ApcSimulationFacade;
import com.nextorm.apcmodeling.service.ApcSimulationListService;
import com.nextorm.apcmodeling.service.ApcSimulationService;
import com.nextorm.apcmodeling.service.interfaces.ApcTargetDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/apc/simulations")
public class ApcSimulationController {
	private final ApcTargetDataService apcTargetDataService;
	private final ApcSimulationService apcSimulationService;
	private final ApcSimulationDataService apcSimulationDataService;
	private final ApcSimulationListService apcSimulationListService;
	private final ApcSimulationFacade apcSimulationFacade;

	public ApcSimulationController(
		@Qualifier("apcTargetDataFromSfpService") ApcTargetDataService apcTargetDataService,
		ApcSimulationService apcSimulationService,
		ApcSimulationDataService apcSimulationDataService,
		ApcSimulationListService apcSimulationListService,
		ApcSimulationFacade apcSimulationFacade
	) {
		this.apcTargetDataService = apcTargetDataService;
		this.apcSimulationService = apcSimulationService;
		this.apcSimulationDataService = apcSimulationDataService;
		this.apcSimulationListService = apcSimulationListService;
		this.apcSimulationFacade = apcSimulationFacade;
	}

	@GetMapping(path = "")
	public List<ApcSimulationListResponseDto> getApcSimulationList(ApcSimulationListSearchRequestDto searchRequestDto) {
		return apcSimulationListService.getApcSimulationList(searchRequestDto);
	}

	@GetMapping(path = "/{apcSimulationId}")
	public ApcSimulationStatusDto getApcSimulation(
		@PathVariable(name = "apcSimulationId") Long apcSimulationId
	) {
		return apcSimulationService.getApcSimulationById(apcSimulationId);
	}

	@GetMapping(path = "/target-data")
	public List<ApcTargetDataDto> getApcSimulationTargetData(
		@RequestParam(name = "from") LocalDateTime from,
		@RequestParam(name = "to") LocalDateTime to
	) {
		return apcTargetDataService.getApcTargetData(from, to);
	}

	@PostMapping(path = "/{apcModelVersionId}/run")
	public ApcSimulationDto runApcSimulation(
		@PathVariable(name = "apcModelVersionId") Long apcModelVersionId,
		@RequestBody List<ApcTargetDataDto> apcTargetDatas
	) {
		return apcSimulationFacade.runSimulation(apcModelVersionId, apcTargetDatas);
	}

	@GetMapping(path = "/data/{apcSimulationDataId}/result")
	public List<ApcRequestResultDto> getApcSimulationDataRequestResult(
		@PathVariable(name = "apcSimulationDataId") Long apcSimulationDataId
	) {
		return apcSimulationDataService.getApcSimulationDataRequestResult(apcSimulationDataId);
	}

	@PutMapping(path = "/{apcSimulationId}/cancel")
	public ApcSimulationDto cancelApcSimulation(
		@PathVariable(name = "apcSimulationId") Long apcSimulationId
	) {
		return apcSimulationFacade.cancelSimulation(apcSimulationId);
	}
}
