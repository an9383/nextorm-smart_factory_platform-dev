package com.nextorm.portal.service.dcpconfig;

import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.dcpconfig.DcpConfigErrorCode;
import com.nextorm.portal.common.exception.dcpconfig.DcpConfigNotFoundException;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.common.exception.tool.ToolErrorCode;
import com.nextorm.portal.dto.dcpconfig.DcpConfigCreateRequestDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigUpdateRequestDto;
import com.nextorm.portal.service.DcpConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DcpConfigExceptionTest {

	@InjectMocks
	DcpConfigService dcpConfigService;

	@Mock
	DcpConfigRepository dcpConfigRepository;

	@Mock
	ToolRepository toolRepository;

	@DisplayName("createDcpConfig 요청 시 잘못된 Tool ID를 요청했을 경우 :RelateToolException")
	@Test
	void givenDcpConfigCreateRequestDtoThenRelateToolExceptionWhenCreateDcpConfig() {

		DcpConfigCreateRequestDto request = DcpConfigCreateRequestDto.builder()
																	 .toolId(10000L)
																	 .topic("topic")
																	 .bootstrapServer("22")
																	 .command("command")
																	 .dataInterval(30)
																	 .collectorType("PROCESS")
																	 .build();

		assertThatThrownBy(() -> dcpConfigService.createDcpConfig(request)).isInstanceOf(RelateToolNotFoundException.class)
																		   .hasMessage(ToolErrorCode.RELATE_TOOL_NOT_FOUND.getMessage());
	}

	@DisplayName("modifyDcpConfig 요청 시 잘못된 DcpConfig ID를 요청했을 경우 :DcpConfigNotFoundException")
	@Test
	void givenDcpConfigUpdateRequestDtoAndIdThenDcpConfigNotFoundExceptionWhenModifyDcpConfig() {

		DcpConfigUpdateRequestDto request = DcpConfigUpdateRequestDto.builder()
																	 .bootstrapServer("22")
																	 .collectorType("PROCESS")
																	 .build();
		assertThatThrownBy(() -> dcpConfigService.modifyDcpConfig(10000L, request)).isInstanceOf(
																					   DcpConfigNotFoundException.class)
																				   .hasMessage(DcpConfigErrorCode.DCP_CONFIG_NOT_FOUND.getMessage());
	}

	@DisplayName("deleteDcpConfig 요청 시 잘못된 DcpConfig ID를 요청했을 경우 :DcpConfigNotFoundException")
	@Test
	void givenIdThenDcpConfigNotFoundExceptionWhenDeleteDcpConfig() {

		assertThatThrownBy(() -> dcpConfigService.deleteDcpConfig(10000L)).isInstanceOf(DcpConfigNotFoundException.class)
																		  .hasMessage(DcpConfigErrorCode.DCP_CONFIG_NOT_FOUND.getMessage());
	}

}
