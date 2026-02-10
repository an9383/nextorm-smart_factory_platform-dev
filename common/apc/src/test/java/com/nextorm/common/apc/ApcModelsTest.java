package com.nextorm.common.apc;

import com.nextorm.common.apc.entity.ApcModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ApcModelsTest {

	@DisplayName("가장 일치도가 높은 모델을 찾는다")
	@ParameterizedTest(name = "{index}: target={0}, expected={2}")
	@MethodSource("findMostSpecificMatchTestSource")
	void findMostSpecificMatchTest(
		String target,
		List<ApcModel> keys,
		String expected
	) {
		ApcModels modelFilter = new ApcModels(keys);
		Optional<ApcModel> mostSpecificMatch = modelFilter.findMostSpecificMatch(target);

		log.info("Most specific match: {}",
			mostSpecificMatch.isPresent()
			? mostSpecificMatch.get()
							   .getCondition()
			: "None");

		assertThat(mostSpecificMatch).isPresent();
		assertThat(mostSpecificMatch.get()
									.getCondition()).isEqualTo(expected);
	}

	@DisplayName("컨디션에 맞는 모든 모델을 찾는다 (1개 반환 확인)")
	@Test
	void 조건에맞는_모든_모델반환_하나로_확인() {
		List<ApcModel> models = List.of(createApcModel("*|;*|;*|;*"),
			createApcModel("eqp1|;*|;*|;*"),
			createApcModel("eqp2|;*|;lot1|;*"),
			createApcModel("eqp2|;step1|;*|;*"),
			createApcModel("eqp2|;step1|;*|;wafer1"),
			createApcModel("eqp3|;step2|;lot1|;*"),
			createApcModel("eqp4|;step3|;lot2|;wafer1"),
			createApcModel("e*|;s*2|;lot1|;wafer999"),
			createApcModel("*999|;*tep*|;lot1|;wafer999"),
			createApcModel("*|;step7|;lot7|;wafer7"));

		ApcModels modelFilter = new ApcModels(models);

		String targetCondition = "asdf|;asdf|;asdf|;asdf";
		List<ApcModel> matchedModels = modelFilter.findMatchedModels(targetCondition);

		assertThat(matchedModels).hasSize(1);
		assertThat(matchedModels.get(0)
								.getCondition()).isEqualTo("*|;*|;*|;*");
	}

	@DisplayName("컨디션에 맞는 모든 모델을 찾는다 (여러개 반환 확인)")
	@Test
	void 조건에맞는_모든_모델반환_여러개반환_확인() {
		List<ApcModel> models = List.of(createApcModel("*|;*|;*|;*"),
			createApcModel("eqp1|;*|;*|;*"),
			createApcModel("eqp2|;*|;lot1|;*"),
			createApcModel("eqp2|;step1|;*|;*"),
			createApcModel("eqp2|;step1|;*|;wafer1"),
			createApcModel("eqp3|;step2|;lot1|;*"),
			createApcModel("eqp4|;step3|;lot2|;wafer1"),
			createApcModel("e*|;s*2|;lot1|;wafer999"),
			createApcModel("*999|;*tep*|;lot1|;wafer999"),
			createApcModel("*|;step7|;lot7|;wafer7"));

		ApcModels modelFilter = new ApcModels(models);

		String targetCondition = "eqp2|;step2|;lot1|;nonono";
		List<ApcModel> matchedModels = modelFilter.findMatchedModels(targetCondition);
		List<String> matchedConditions = matchedModels.stream()
													  .map(ApcModel::getCondition)
													  .toList();

		assertThat(matchedModels).hasSize(2);
		assertThat(matchedConditions).containsAll(List.of("eqp2|;*|;lot1|;*", "*|;*|;*|;*"));
	}

	/**
	 * {@link ApcModelsTest#findMostSpecificMatchTest(String, List, String)} 테스트 데이터 소스
	 *
	 * @return [target, keys, expected]
	 */
	static Stream<Arguments> findMostSpecificMatchTestSource() {
		List<ApcModel> keys = List.of(createApcModel("*|;*|;*|;*"),
			createApcModel("eqp1|;*|;*|;*"),
			createApcModel("eqp2|;*|;lot1|;*"),
			createApcModel("eqp2|;step1|;*|;*"),
			createApcModel("eqp2|;step1|;*|;wafer1"),
			createApcModel("eqp3|;step2|;lot1|;*"),
			createApcModel("eqp4|;step3|;lot2|;wafer1"),
			createApcModel("e*|;s*2|;lot1|;wafer999"),
			createApcModel("*999|;*tep*|;lot1|;wafer999"),
			createApcModel("*|;step7|;lot7|;wafer7"));

		return Stream.of(Arguments.of("eqp2|;step1|;lot1|;wafer1", keys, "eqp2|;step1|;*|;wafer1"),
			Arguments.of("eqp4|;step3|;lot2|;wafer9999", keys, "*|;*|;*|;*"),
			Arguments.of("eqp99|;step2|;lot1|;wafer999", keys, "e*|;s*2|;lot1|;wafer999"),
			Arguments.of("eeee999|;sstep000|;lot1|;wafer999", keys, "*999|;*tep*|;lot1|;wafer999"),
			Arguments.of("eqp1|;|;|;novalue", keys, "eqp1|;*|;*|;*"));
	}

	static ApcModel createApcModel(String condition) {
		return ApcModel.builder()
					   .condition(condition)
					   .build();
	}
}
