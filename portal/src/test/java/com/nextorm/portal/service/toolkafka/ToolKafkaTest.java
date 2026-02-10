package com.nextorm.portal.service.toolkafka;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.ToolKafka;
import com.nextorm.common.db.repository.*;
import com.nextorm.portal.dto.tool.ToolKafkaCreateRequestDto;
import com.nextorm.portal.dto.tool.ToolKafkaUpdateRequestDto;
import com.nextorm.portal.event.RefreshMessageService;
import com.nextorm.portal.service.ToolService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ToolKafkaTest {

	@InjectMocks
	ToolService toolService;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	ToolRepository toolRepository;

	@Mock
	ToolKafkaRepository toolKafkaRepository;

	@Mock
	ParameterRepository parameterRepository;

	@Mock
	DcpConfigRepository dcpConfigRepository;

	@Mock
	LocationRepository locationRepository;

	@Mock
	RefreshMessageService refreshMessageService;

	@DisplayName("create ToolKafka")
	@Test
	void givenToolKafkaCreateRequestDtoWhenCreateToolKafka() {
		Tool tool = Tool.builder()
						.name("test")
						.id(1L)
						.build();

		when(toolRepository.findById(1L)).thenReturn(Optional.ofNullable(tool));

		ToolKafkaCreateRequestDto requestDto = new ToolKafkaCreateRequestDto("test");

		Assertions.assertThatCode(() -> toolService.createToolKafka(1L, requestDto))
				  .doesNotThrowAnyException();

	}

	@DisplayName("ToolKafka modify")
	@Test
	void givenToolKafkaUpdataRequestDtoWhenModifyToolKafka() {
		Tool tool = Tool.builder()
						.name("test")
						.id(1L)
						.build();

		ToolKafka toolKafka = ToolKafka.builder()
									   .toolId(tool.getId())
									   .id(1L)
									   .bootstrapServer("test")
									   .build();

		when(toolKafkaRepository.findByToolId(1L)).thenReturn(Optional.ofNullable(toolKafka));

		ToolKafkaUpdateRequestDto requestDto = new ToolKafkaUpdateRequestDto("test123");

		Assertions.assertThatCode(() -> toolService.modifyToolKafka(1L, requestDto))
				  .doesNotThrowAnyException();
	}
}
