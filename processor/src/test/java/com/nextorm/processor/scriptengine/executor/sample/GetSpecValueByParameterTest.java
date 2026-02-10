package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.processor.model.ParameterModel;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

class GetSpecValueByParameterTest {

	@DisplayName("파라미터가 있다면 스펙값을 반환")
	@ParameterizedTest
	@CsvSource({"UCL, 10.0", "USL, 5.0", "LSL, -5.0", "LCL, -10.0"})
	void getSpecValueTest_success(
		String specType,
		Double expectedSpecValue
	) {
		// given
		ParameterContainer parameterContainer = Mockito.mock(ParameterContainer.class);
		ParameterModel parameter = ParameterModel.builder()
												 .id(1L)
												 .ucl(10.0d)
												 .usl(5.0d)
												 .lsl(-5.0d)
												 .lcl(-10.0d)
												 .build();

		GetSpecValueByParameter executor = new GetSpecValueByParameter(parameterContainer);

		when(parameterContainer.getParameterById(parameter.getId())).thenReturn(parameter);

		// when
		Double specValue = executor.getSpecValue("1", specType);

		// then
		assertThat(specValue).isEqualTo(expectedSpecValue);
	}

	@DisplayName("파라미터가 있지만, 파라미터에 스펙값이 없으면 null을 반환")
	@ParameterizedTest
	@CsvSource({"UCL", "USL", "LSL", "LCL"})
	void getSpecValueTest_null_value_success(
		String specType
	) {
		// given
		ParameterContainer parameterContainer = Mockito.mock(ParameterContainer.class);
		ParameterModel parameter = ParameterModel.builder()
												 .id(1L)
												 .build();

		GetSpecValueByParameter executor = new GetSpecValueByParameter(parameterContainer);

		when(parameterContainer.getParameterById(parameter.getId())).thenReturn(parameter);

		// when
		Double specValue = executor.getSpecValue("1", specType);

		// then
		assertThat(specValue).isNull();
	}

	@DisplayName("스펙 타입 문자열이 대소문자 상관없이 값을 반환")
	@ParameterizedTest
	@CsvSource({"Ucl, 10.0", "uSl, 5.0", "lsL, -5.0", "LCL, -10.0", "lcl, -10.0"})
	void getSpecValueTest_type_case_success(
		String specType,
		Double expectedSpecValue
	) {
		// given
		ParameterContainer parameterContainer = Mockito.mock(ParameterContainer.class);
		ParameterModel parameter = ParameterModel.builder()
												 .id(1L)
												 .ucl(10.0d)
												 .usl(5.0d)
												 .lsl(-5.0d)
												 .lcl(-10.0d)
												 .build();

		GetSpecValueByParameter executor = new GetSpecValueByParameter(parameterContainer);

		when(parameterContainer.getParameterById(parameter.getId())).thenReturn(parameter);

		// when
		Double specValue = executor.getSpecValue("1", specType);

		// then
		assertThat(specValue).isEqualTo(expectedSpecValue);
	}

	@DisplayName("파라미터를 찾지 못하면 null을 반환")
	@Test
	void getSpecValueTest_if_not_found_parameter_null_success() {
		// given
		ParameterContainer parameterContainer = Mockito.mock(ParameterContainer.class);
		GetSpecValueByParameter executor = new GetSpecValueByParameter(parameterContainer);

		when(parameterContainer.getParametersByToolId(1L)).thenReturn(null);

		// when
		Double specValue = executor.getSpecValue("1", "LCL");

		// then
		assertThat(specValue).isNull();
	}

}