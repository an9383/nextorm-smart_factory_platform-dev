package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.common.exception.apcmodelsimulationdata.ApcModelSimulationDataErrorCode;
import com.nextorm.apcmodeling.common.exception.apcmodelsimulationdata.ApcSimulationDataNotFoundException;
import com.nextorm.common.apc.repository.ApcModelSimulationDataRepository;
import com.nextorm.common.apc.repository.ApcRequestResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class ApcSimulationDataExceptionTest {

	@InjectMocks
	ApcSimulationDataService apcSimulationDataService;

	@Mock
	ApcModelSimulationDataRepository apcModelSimulationDataRepository;

	@Mock
	ApcRequestResultRepository apcRequestResultRepository;

	@Test
	@DisplayName("getApcSimulationDataRequestResult 요청 시 잘못된 apcModelSimulationDataId를 요청했을 경우: ApcSimulationDataNotFoundException")
	void givenApcModelSimulationIdThenApcSimulationDataNotFoundExceptionWhenGetApcSimulationDataRequestResult() {
		assertThatCode(() -> apcSimulationDataService.getApcSimulationDataRequestResult(1111111L)).isInstanceOf(
																									  ApcSimulationDataNotFoundException.class)
																								  .hasMessage(
																									  ApcModelSimulationDataErrorCode.APC_MODEL_SIMULATION_DATA_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("sentApcSimulationData 요청 시 잘못된 apcModelSimulationDataId를 요청했을 경우: ApcSimulationDataNotFoundException")
	void givenApcModelSimulationIdThenApcSimulationDataNotFoundExceptionWhenSentApcSimulationData() {
		assertThatCode(() -> apcSimulationDataService.sentApcSimulationData(11111111L)).isInstanceOf(
																						   ApcSimulationDataNotFoundException.class)
																					   .hasMessage(
																						   ApcModelSimulationDataErrorCode.APC_MODEL_SIMULATION_DATA_NOT_FOUND.getMessage());
	}
}
