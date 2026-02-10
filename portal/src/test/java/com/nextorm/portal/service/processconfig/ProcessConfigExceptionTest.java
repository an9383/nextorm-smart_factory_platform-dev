package com.nextorm.portal.service.processconfig;

import com.nextorm.common.db.entity.ProcessConfig;
import com.nextorm.common.db.repository.ProcessConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.processconfig.ProcessConfigErrorCode;
import com.nextorm.portal.common.exception.processconfig.ProcessConfigNameDuplicationException;
import com.nextorm.portal.common.exception.processconfig.ProcessConfigNotFoundException;
import com.nextorm.portal.dto.processconfig.ProcessConfigCreateRequestDto;
import com.nextorm.portal.dto.processconfig.ProcessConfigUpdateRequestDto;
import com.nextorm.portal.service.ProcessConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessConfigExceptionTest {

	@InjectMocks
	ProcessConfigService processConfigService;

	@Mock
	ProcessConfigRepository processConfigRepository;

	@Mock
	ToolRepository toolRepository;

	@DisplayName("createProcessConfig 요청 시 중복되는 이름이 있을 경우: ProcessConfigNameDuplicationException")
	@Test
	void givenProcessConfigCreateRequestDtoThenProcessConfigNameDuplicationExceptionWhenCreateProcessConfig() {

		ProcessConfig processConfig = ProcessConfig.builder()
												   .name("test")
												   .build();
		when(processConfigRepository.findOneByName("test")).thenReturn(Optional.of(processConfig));

		ProcessConfigCreateRequestDto request = new ProcessConfigCreateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> processConfigService.createProcessConfig(request)).isInstanceOf(
																					   ProcessConfigNameDuplicationException.class)
																				   .hasMessage(ProcessConfigErrorCode.PROCESS_CONFIG_DUPLICATION.getMessage());
	}

	@DisplayName("modifyProcessConfig 요청 시 Id가 잘못 요청되었을 경우: ProcessConfigUnProcessableException")
	@Test
	void givenConfigIdAndProcessConfigUpdateRequestDtoThenProcessConfigUnProcessableExceptionWhenmodifyProcessConfig() {
		ProcessConfigUpdateRequestDto request = new ProcessConfigUpdateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> processConfigService.modifyProcessConfig(1111111L, request)).isInstanceOf(
																								 ProcessConfigNotFoundException.class)
																							 .hasMessage(
																								 ProcessConfigErrorCode.PROCESS_CONFIG_NOT_FOUND.getMessage());
	}
}
