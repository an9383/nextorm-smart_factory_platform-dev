package com.nextorm.common.db.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterTest {

	@DisplayName("파라미터의 데이터 타입이 숫자인지 확인한다")
	@ParameterizedTest
	@CsvSource({"DOUBLE, true", "INTEGER, true", "STRING, false"})
	void isNumberType_test(
		Parameter.DataType type,
		boolean expected
	) {
		Parameter parameter = Parameter.builder()
									   .dataType(type)
									   .build();

		boolean result = parameter.isNumberType();

		assertThat(result).isEqualTo(expected);
	}

	@DisplayName("자동 스펙 계산 기간이 NULL인 경우, 잘못 설정된 것으로 판단한다")
	@Test
	void isInvalidAutoCalcPeriod_null_test() {
		Parameter parameter = Parameter.builder()
									   .autoCalcPeriod(null)
									   .build();

		boolean result = parameter.isInvalidAutoCalcPeriod();

		assertThat(result).isTrue();
	}

	@DisplayName("자동 스펙 계산 기간이 0이하인 경우, 잘못 설정된 것으로 판단한다")
	@ParameterizedTest
	@ValueSource(ints = {0, -1, -5, -10, -20, -100})
	void isInvalidAutoCalcPeriod_0_test(Integer autoCalcPeriod) {
		Parameter parameter = Parameter.builder()
									   .autoCalcPeriod(autoCalcPeriod)
									   .build();

		boolean result = parameter.isInvalidAutoCalcPeriod();

		assertThat(result).isTrue();
	}

	@DisplayName("자동 스펙 계산 기간이 1이상인 경우, 올바르게 설정된 것으로 판단한다")
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 5, 10, 20, 100})
	void isInvalidAutoCalcPeriod_valide_test(Integer autoCalcPeriod) {
		Parameter parameter = Parameter.builder()
									   .autoCalcPeriod(autoCalcPeriod)
									   .build();

		boolean result = parameter.isInvalidAutoCalcPeriod();

		assertThat(result).isFalse();
	}

	@DisplayName("스펙 설정 true, 오토 스펙 false, 데이터가 number 타입이면. 수동 설정은 활성화 된다")
	@ParameterizedTest
	@ValueSource(strings = {"DOUBLE", "INTEGER"})
	void isManualSpecCalculationTarget_true_number(Parameter.DataType dataType) {
		// given
		Parameter parameter = Parameter.builder()
									   .isSpecAvailable(true)
									   .isAutoSpec(false)
									   .dataType(dataType)
									   .build();

		// when
		boolean result = parameter.isManualSpecCalculationTarget();

		// then
		assertThat(result).isTrue();
	}

	@DisplayName("스펙 설정 true, 오토 스펙 false, 데이터가 number가 아니면. 수동 설정은 비활성화 된다")
	@Test
	void isManualSpecCalculationTarget_false_not_number() {
		// given
		Parameter parameter = Parameter.builder()
									   .isSpecAvailable(true)
									   .isAutoSpec(false)
									   .dataType(Parameter.DataType.STRING)
									   .build();

		// when
		boolean result = parameter.isManualSpecCalculationTarget();

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("스펙설정이 false면 수동 스펙 계산 대상이 아니다")
	@Test
	void isManualSpecCalculationTarget_false_spec() {
		// given
		Parameter parameter = Parameter.builder()
									   .isSpecAvailable(false)
									   .isAutoSpec(false)
									   .dataType(Parameter.DataType.DOUBLE)
									   .build();

		// when
		boolean result = parameter.isManualSpecCalculationTarget();

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("자동 스펙이 true면 수동 스펙 계산 대상이 아니다")
	@Test
	void isManualSpecCalculationTarget_false_autoSpec() {
		// given
		Parameter parameter = Parameter.builder()
									   .isSpecAvailable(true)
									   .isAutoSpec(true)
									   .dataType(Parameter.DataType.DOUBLE)
									   .build();

		// when
		boolean result = parameter.isManualSpecCalculationTarget();

		// then
		assertThat(result).isFalse();
	}

	@Test
	void isAutoSpecCalculationTarget() {
	}
}
