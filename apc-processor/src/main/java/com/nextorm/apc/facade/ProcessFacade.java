package com.nextorm.apc.facade;

import com.nextorm.apc.dto.response.ProcessResponseDto;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessFacade {
	private final ApcRequestService apcRequestService;
	private final ScriptEngineExecutorService scriptEngineExecutorService;
	private final ResultHandler resultHandler;

	public ProcessResponseDto process(Map<String, Object> source) {
		Long apcRequestId = null;

		try {
			ApcRequest apcRequest = apcRequestService.initProcessRequest(source);
			apcRequestId = apcRequest.getId();

			if (!apcRequest.isRunning()) {
				return ProcessResponseDto.fail(apcRequestId);
			}

			EngineExecuteResult executeResult = scriptEngineExecutorService.execute(apcRequestId);
			resultHandler.handleSuccess(apcRequestId, executeResult);
			return new ProcessResponseDto(apcRequestId, executeResult.isSuccess());
		} catch (RuntimeException e) {
			log.info("Process failed. source: {}", source, e);
			BusinessException businessException = e instanceof BusinessException
												  ? (BusinessException)e
												  : new NoneHandlingException(e);

			if (apcRequestId == null) {
				Long errorRequestId = resultHandler.handleRequestInitBeforeError(ApcRequest.Type.SIMULATION,
					businessException);
				return ProcessResponseDto.fail(errorRequestId);
			}

			resultHandler.handleError(apcRequestId, businessException);
			return ProcessResponseDto.fail(apcRequestId);
		}
	}
}
