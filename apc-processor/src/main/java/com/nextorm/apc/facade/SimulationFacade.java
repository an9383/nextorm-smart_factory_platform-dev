package com.nextorm.apc.facade;

import com.nextorm.apc.dto.response.SimulationResponseDto;
import com.nextorm.apc.exception.BusinessException;
import com.nextorm.apc.exception.NoneHandlingException;
import com.nextorm.apc.scriptengine.EngineExecuteResult;
import com.nextorm.apc.service.ApcRequestService;
import com.nextorm.apc.service.ResultHandler;
import com.nextorm.apc.service.ScriptEngineExecutorService;
import com.nextorm.common.apc.entity.ApcRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationFacade {
	private final ApcRequestService apcRequestService;
	private final ScriptEngineExecutorService scriptEngineExecutorService;
	private final ResultHandler resultHandler;

	public SimulationResponseDto simulate(Long simulationDataId) {
		Long apcRequestId = null;
		try {
			ApcRequest apcRequest = apcRequestService.initSimulationRequest(simulationDataId);
			apcRequestId = apcRequest.getId();
			if (!apcRequest.isRunning()) {
				return SimulationResponseDto.fail(apcRequestId);
			}

			EngineExecuteResult executeResult = scriptEngineExecutorService.execute(apcRequestId);
			resultHandler.handleSimulationSuccess(simulationDataId, apcRequestId, executeResult);
			return new SimulationResponseDto(apcRequestId, executeResult.isSuccess());
		} catch (RuntimeException e) {
			log.error("Simulation failed. simulationDataId: {}", simulationDataId, e);
			BusinessException businessException = e instanceof BusinessException
												  ? (BusinessException)e
												  : new NoneHandlingException(e);
			if (apcRequestId == null) {
				Long errorRequestId = resultHandler.handleRequestInitBeforeError(ApcRequest.Type.SIMULATION,
					simulationDataId,
					businessException);
				return SimulationResponseDto.fail(errorRequestId);
			}
			resultHandler.handleError(apcRequestId, businessException);
			return SimulationResponseDto.fail(apcRequestId);
		}
	}
}
