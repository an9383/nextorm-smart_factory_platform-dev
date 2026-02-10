package com.nextorm.portal.service.parameter;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.dcpconfig.DcpConfigErrorCode;
import com.nextorm.portal.common.exception.dcpconfig.RelateDcpConfigNotFoundException;
import com.nextorm.portal.common.exception.parameter.ParameterErrorCode;
import com.nextorm.portal.common.exception.parameter.ParameterNotFoundException;
import com.nextorm.portal.common.exception.parameter.RelateParameterNotFoundException;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.common.exception.tool.ToolErrorCode;
import com.nextorm.portal.dto.parameter.ParameterCreateRequestDto;
import com.nextorm.portal.dto.parameter.ParameterUpdateRequestDto;
import com.nextorm.portal.dto.parameter.VirtualParameterCreateRequestDto;
import com.nextorm.portal.dto.parameter.VirtualParameterUpdateRequestDto;
import com.nextorm.portal.service.ParameterDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParameterExceptionTest {
	@InjectMocks
	ParameterService parameterService;

	@Mock
	ParameterDataService parameterDataService;

	@Mock
	ParameterRepository parameterRepository;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	DcpConfigRepository dcpConfigRepository;

	@Mock
	ToolRepository toolRepository;

	@BeforeEach
	void makeEntityGetVirtualParameterById() {

	}

	@DisplayName("getParameterByNameAndToolId 요청 시 잘못된 ToolId, name을 요청한 경우: ParameterNotFoundException")
	@Test
	void givenParameterNameAndToolIdThenParameterNotFoundExceptionWhenGetParameterByNameAndToolId() {
		assertThatThrownBy(() -> parameterService.getParameterByNameAndToolId(1111111111L, "test")).isInstanceOf(
																									   ParameterNotFoundException.class)
																								   .hasMessage(
																									   ParameterErrorCode.PARAMETER_NOT_FOUND.getMessage());

	}

	@DisplayName("getParameter 요청 시 잘못된 ParameterId를 요청했을 경우: ParameterNotFoundException")
	@Test
	void givenParameterIdThenParameterNotFoundExceptionWhenGetParameter() {
		assertThatThrownBy(() -> parameterService.getParameter(111111111111L)).isInstanceOf(ParameterNotFoundException.class)
																			  .hasMessage(ParameterErrorCode.PARAMETER_NOT_FOUND.getMessage());
	}

	@DisplayName("createParameter 요청 시 절못된 ToolId를 요청했을 경우: RelateToolNotFoundException")
	@Test
	void givenToolIdThenRelateToolNotFoundExceptionWhenCreateParameter() {
		ParameterCreateRequestDto request = ParameterCreateRequestDto.builder()
																	 .name("test")
																	 .toolId(1111111111111111L)
																	 .build();

		assertThatThrownBy(() -> parameterService.createParameter(request)).isInstanceOf(RelateToolNotFoundException.class)
																		   .hasMessage(ToolErrorCode.RELATE_TOOL_NOT_FOUND.getMessage());
	}

	@DisplayName("modifyParameter 요청 시 잘못된 ParameterId를 요청했을 경우: ParameterNotFoundException")
	@Test
	void givenParameterIdThenParameterNotFoundExceptionWhenModifyParameter() {
		ParameterUpdateRequestDto request = ParameterUpdateRequestDto.builder()
																	 .name("test")
																	 .build();

		assertThatThrownBy(() -> parameterService.modifyParameter(11111L, request)).isInstanceOf(
																					   ParameterNotFoundException.class)
																				   .hasMessage(ParameterErrorCode.PARAMETER_NOT_FOUND.getMessage());
	}

	@DisplayName("deleteParameter 요청 시 잘못된 ParameterId를 요청했을 경우: ParameterNotFoundException")
	@Test
	void givenParameterIdThenParameterNotFoundExceptionWhenDeleteParameter() {
		assertThatThrownBy(() -> parameterService.deleteParameter(111111L)).isInstanceOf(ParameterNotFoundException.class)
																		   .hasMessage(ParameterErrorCode.PARAMETER_NOT_FOUND.getMessage());
	}

	@DisplayName("createVirtualParameter 요청 시 잘못된 ToolId를 요청했을 경우: RelateToolNotFoundException")
	@Test
	void givenToolIdThenRelateToolNotFoundExceptionWhenCreateVirtualParameter() {
		VirtualParameterCreateRequestDto request = new VirtualParameterCreateRequestDto();
		request.setToolId(11111111L);
		request.setName("test");

		assertThatThrownBy(() -> parameterService.createVirtualParameter(request)).isInstanceOf(
																					  RelateToolNotFoundException.class)
																				  .hasMessage(ToolErrorCode.RELATE_TOOL_NOT_FOUND.getMessage());
	}

	@DisplayName("getVirtualParameterById 요청 시 잘못된 ParameterId를 요청했을 경우: ParameterNotFoundException")
	@Test
	void givenParameterIdThenParameterNotFoundExceptionWhenGetVirtualParameter() {

		assertThatThrownBy(() -> parameterService.getVirtualParameterById(111111L)).isInstanceOf(
																					   RelateParameterNotFoundException.class)
																				   .hasMessage(ParameterErrorCode.RELATE_PARAMETER_NOT_FOUND.getMessage());
	}

	@DisplayName("getVirtualParameterById 요청 시 잘못된 ParameterId를 요청했을 경우: RelateDcpConfigNotFoundException")
	@Test
	void givenParameterIdThenRelateDcpConfigNotFoundExceptionWhenGetVirtualParameter() {
		Parameter parameter = Parameter.builder()
									   .id(130L)
									   .name("test")
									   .build();
		when(parameterRepository.findByIdWithMappingParameters(130L)).thenReturn(Optional.of(parameter));

		VirtualParameterUpdateRequestDto request = new VirtualParameterUpdateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> parameterService.getVirtualParameterById(130L)).isInstanceOf(
																					RelateDcpConfigNotFoundException.class)
																				.hasMessage(DcpConfigErrorCode.RELATE_DCP_CONFIG_NOT_FOUND.getMessage());
	}

	@DisplayName("modifyVirtualParameter 요청 시 잘못된 ParameterId를 요청했을 경우: ParameterNotFoundException")
	@Test
	void givenParameterIdThenParameterNotFoundExceptionWhenModifyVirtualParameter() {
		VirtualParameterUpdateRequestDto request = new VirtualParameterUpdateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> parameterService.modifyVirtualParameter(111111L, request)).isInstanceOf(
																							   RelateParameterNotFoundException.class)
																						   .hasMessage(
																							   ParameterErrorCode.RELATE_PARAMETER_NOT_FOUND.getMessage());
	}

	@DisplayName("modifyVirtualParameter 요청 시 잘못된 ParameterId를 요청했을 경우: RelateDcpConfigNotFoundException")
	@Test
	void givenParameterIdThenRelateDcpConfigNotFoundExceptionWhenModifyVirtualParameter() {
		Parameter parameter = Parameter.builder()
									   .id(130L)
									   .name("test")
									   .build();
		when(parameterRepository.findByIdWithMappingParameters(130L)).thenReturn(Optional.of(parameter));

		VirtualParameterUpdateRequestDto request = new VirtualParameterUpdateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> parameterService.modifyVirtualParameter(parameter.getId(), request)).isInstanceOf(
																										 RelateDcpConfigNotFoundException.class)
																									 .hasMessage(
																										 DcpConfigErrorCode.RELATE_DCP_CONFIG_NOT_FOUND.getMessage());
	}

}
