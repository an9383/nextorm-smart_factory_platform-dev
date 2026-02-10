package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.dto.ApcSimulationDataDto;
import com.nextorm.apcmodeling.dto.ApcSimulationDto;
import com.nextorm.apcmodeling.dto.ApcTargetDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApcSimulationFacade {

	private final ApcSimulationTaskService apcSimulationTaskService;
	private final ApcSimulationService apcSimulationService;

	/**
	 * APC Model Simulation 실행
	 *
	 * @param apcModelVersionId APC 모델 버전 ID
	 * @param apcTargetDatas    Simulation 타겟 데이터 목록
	 * @return
	 */
	public ApcSimulationDto runSimulation(
		Long apcModelVersionId,
		List<ApcTargetDataDto> apcTargetDatas
	) {
		//APC 시뮬레이션 저장
		ApcSimulationDto apcSimulation = apcSimulationService.saveApcSimulation(apcModelVersionId, apcTargetDatas);

		List<Long> simulationDataIds = apcSimulation.getApcSimulationDatas()
													.stream()
													.map(ApcSimulationDataDto::getId)
													.toList();
		//비동기로 Simulation Data 요청
		apcSimulationTaskService.runTask(apcSimulation.getId(), simulationDataIds);

		return apcSimulation;
	}

	/**
	 * 시뮬레이션 취소
	 *
	 * @param apcSimulationId APC 시뮬레이션 ID
	 * @return
	 */
	public ApcSimulationDto cancelSimulation(Long apcSimulationId) {
		apcSimulationTaskService.cancelTask(apcSimulationId);
		return apcSimulationService.cancelApcSimulation(apcSimulationId);
	}
}
