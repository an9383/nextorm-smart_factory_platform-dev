package com.nextorm.portal.service.summaryconfig;

import com.nextorm.common.db.entity.SummaryConfig;
import com.nextorm.common.db.repository.SummaryConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.summaryconfig.SummaryConfigErrorCode;
import com.nextorm.portal.common.exception.summaryconfig.SummaryConfigNameDuplicationException;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigCreateRequestDto;
import com.nextorm.portal.service.SummaryConfigService;
import org.junit.jupiter.api.BeforeEach;
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
class SummaryConfigExceptionTest {

	@InjectMocks
	SummaryConfigService summaryConfigService;

	@Mock
	SummaryConfigRepository summaryConfigRepository;

	@Mock
	ToolRepository toolRepository;
	
	@BeforeEach
	void makeEntity() {
		SummaryConfig summaryConfig = SummaryConfig.builder()
												   .name("test-summary-config")
												   .build();
		when(summaryConfigRepository.findOneByName(summaryConfig.getName())).thenReturn(Optional.of(summaryConfig));
	}

	@DisplayName("createSummaryConfig 요청 시 중복된 이름을 요청했을 경우: SummaryConfigNameDuplicationException")
	@Test
	void givenSummaryConfigCreateRequestDtoThenSummaryConfigNameDuplicationExceptionWhenCreateSummaryConfig() {
		SummaryConfigCreateRequestDto request = new SummaryConfigCreateRequestDto();
		request.setName("test-summary-config");

		assertThatThrownBy(() -> summaryConfigService.createSummaryConfig(request)).isInstanceOf(
																					   SummaryConfigNameDuplicationException.class)
																				   .hasMessage(SummaryConfigErrorCode.SUMMARY_NAME_DUPLICATION.getMessage());

	}

}
