package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.common.exception.apcmodel.ApcModelErrorCode;
import com.nextorm.apcmodeling.common.exception.apcmodel.ApcModelNameDuplicateException;
import com.nextorm.apcmodeling.common.exception.apcmodel.ApcModelNotFoundException;
import com.nextorm.apcmodeling.dto.ApcModelCreateRequestDto;
import com.nextorm.apcmodeling.dto.ApcModelUpdateRequestDto;
import com.nextorm.common.apc.entity.ApcModel;
import com.nextorm.common.apc.repository.ApcModelRepository;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApcModelExceptionTest {

	@InjectMocks
	ApcModelService apcModelService;

	@Mock
	ApcModelRepository apcModelRepository;

	@Mock
	ApcModelVersionRepository apcModelVersionRepository;

	@Test
	@DisplayName("createApcModelVersion 요청 시 중복된 이름을 요청했을 경우: ApcModelNameDuplicateException")
	void givenApcModelVersionCreateRequestDtoThenApcModelNameDuplicateExceptionWhenCreateApcModelVersion() {
		ApcModel mock = ApcModel.builder()
								.modelName("test")
								.isUse(true)
								.id(1L)
								.build();
		when(apcModelRepository.findByModelNameAndIsDelete("test", false)).thenReturn(mock);

		ApcModelCreateRequestDto request = ApcModelCreateRequestDto.builder()
																   .modelName("test")
																   .isActive(true)
																   .build();

		assertThatCode(() -> apcModelService.createApcModel(request)).isInstanceOf(ApcModelNameDuplicateException.class)
																	 .hasMessage(ApcModelErrorCode.APC_MODEL_NAME_DUPLICATE.getMessage());

	}

	@Test
	@DisplayName("modifyApcModel 요청 시 잘못된 apcModelId를 요청했을 경우: ApcModelNotFoundException")
	void givenApcModelIdAndApcModelUpdateRequestDtoThenApcModelNotFoundExceptionWhenModifyApcModel() {
		ApcModelUpdateRequestDto request = ApcModelUpdateRequestDto.builder()
																   .modelName("testtest")
																   .build();

		assertThatCode(() -> apcModelService.modifyApcModel(1111L,
			request)).isInstanceOf(ApcModelNotFoundException.class)
					 .hasMessage(ApcModelErrorCode.APC_MODEL_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("modifyApcModel 요청 시 잘못된 apcModelId를 요청했을 경우: ApcModelNotFoundException")
	void givenApcModelIdThenApcModelNotFoundExceptionWhenDeleteApcModel() {
		assertThatCode(() -> apcModelService.deleteApcModel(1111L)).isInstanceOf(ApcModelNotFoundException.class)
																   .hasMessage(ApcModelErrorCode.APC_MODEL_NOT_FOUND.getMessage());
	}

}
