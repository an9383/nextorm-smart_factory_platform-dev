package com.nextorm.apc.controller;

import com.nextorm.apc.dto.response.SimulationResponseDto;
import com.nextorm.apc.facade.SimulationFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
	private final SimulationFacade simulationFacade;

	@PostMapping("/{simulationDataId}")
	public SimulationResponseDto simulate(@PathVariable("simulationDataId") Long simulationDataId) {
		log.info("시뮬레이션 요청 Data ID: {}", simulationDataId);
		return simulationFacade.simulate(simulationDataId);
	}

}
