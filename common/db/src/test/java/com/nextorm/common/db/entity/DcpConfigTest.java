package com.nextorm.common.db.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DcpConfigTest {

	@Test
	@DisplayName("팩토리 메서드로 DcpConfig를 생성한다")
	void create_test() {
		// Given
		Tool tool = new Tool();
		List<Parameter> parameters = List.of(new Parameter());
		List<Rule> rules = List.of(new Rule());

		DcpConfig createData = DcpConfig.builder()
										.topic("testTopic")
										.bootstrapServer("testBootstrapServer")
										.command("testCommand")
										.dataInterval(10)
										.build();

		// When
		DcpConfig createdDcpConfig = DcpConfig.create(createData, tool, parameters, rules);

		// Then
		assertThat(createdDcpConfig.getTool()).isEqualTo(tool);
		assertThat(createdDcpConfig.getParameters()).hasSize(parameters.size());
		assertThat(createdDcpConfig.getParameters()).hasSize(parameters.size());
		assertThat(createdDcpConfig.getRules()).hasSize(rules.size());
		assertThat(createdDcpConfig.getTopic()).isEqualTo(createData.getTopic());
		assertThat(createdDcpConfig.getBootstrapServer()).isEqualTo(createData.getBootstrapServer());
		assertThat(createdDcpConfig.getCommand()).isEqualTo(createData.getCommand());
		assertThat(createdDcpConfig.getDataInterval()).isEqualTo(createData.getDataInterval());
		assertThat(createdDcpConfig.getIsUse()).isTrue();
	}

	@Test
	@DisplayName("getParameters 메서드는 올바른 Parameter 리스트를 반환한다")
	void getParameters_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Parameter parameter1 = new Parameter();
		Parameter parameter2 = new Parameter();
		DcpConfigParameterMapping mapping1 = DcpConfigParameterMapping.create(dcpConfig, parameter1);
		DcpConfigParameterMapping mapping2 = DcpConfigParameterMapping.create(dcpConfig, parameter2);
		dcpConfig.getParameterMappings()
				 .add(mapping1);
		dcpConfig.getParameterMappings()
				 .add(mapping2);

		// When
		List<Parameter> parameters = dcpConfig.getParameters();

		// Then
		assertThat(parameters).hasSize(2)
							  .contains(parameter1, parameter2);
	}

	@Test
	@DisplayName("getParameters 메서드는 Parameter 객체가 없을 때 빈 리스트를 반환한다")
	void getParameters_noParameters_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();

		// When
		List<Parameter> parameters = dcpConfig.getParameters();

		// Then
		assertThat(parameters).isEmpty();
	}

	@Test
	@DisplayName("addParameter 메서드는 Parameter 객체를 올바르게 추가한다")
	void addParameter_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Parameter parameter = new Parameter();

		// When
		dcpConfig.addParameter(parameter);

		// Then
		assertThat(dcpConfig.getParameterMappings()).hasSize(1);
		assertThat(dcpConfig.getParameterMappings()
							.get(0)
							.getParameter()).isEqualTo(parameter);
	}

	@Test
	@DisplayName("특정 Parameter 제거 시, DcpConfig의 Parameter 리스트에서 해당 Parameter가 제거된다")
	void removeMultipleParameters_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Parameter parameter1 = Parameter.builder()
										.id(1L)
										.build();
		Parameter parameter2 = Parameter.builder()
										.id(2L)
										.build();
		Parameter parameter3 = Parameter.builder()
										.id(3L)
										.build();
		dcpConfig.addParameter(parameter1);
		dcpConfig.addParameter(parameter2);
		dcpConfig.addParameter(parameter3);

		// When
		dcpConfig.removeParameter(parameter2);

		// Then
		assertThat(dcpConfig.getParameterMappings()).hasSize(2);
		assertThat(dcpConfig.getParameterMappings()
							.get(0)
							.getParameter()).isEqualTo(parameter1);
		assertThat(dcpConfig.getParameterMappings()
							.get(1)
							.getParameter()).isEqualTo(parameter3);
	}

	@Test
	@DisplayName("존재하지 않는 Parameter 제거 시도 시, Dcp Config의 Parameter 리스트는 변하지 않는다")
	void removeNonExistingParameter_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Parameter parameter1 = Parameter.builder()
										.id(1L)
										.build();
		Parameter parameter2 = Parameter.builder()
										.id(2L)
										.build();
		Parameter nonExistingParameter = Parameter.builder()
												  .id(3L)
												  .build();
		dcpConfig.addParameter(parameter1);
		dcpConfig.addParameter(parameter2);

		// When
		dcpConfig.removeParameter(nonExistingParameter);

		// Then
		assertThat(dcpConfig.getParameterMappings()).hasSize(2);
		assertThat(dcpConfig.getParameterMappings()
							.get(0)
							.getParameter()).isEqualTo(parameter1);
		assertThat(dcpConfig.getParameterMappings()
							.get(1)
							.getParameter()).isEqualTo(parameter2);
	}

	@Test
	@DisplayName("addRule 메서드는 Dcp Config에 Rule 객체를 추가한다")
	void addRule_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Rule rule = new Rule();

		// When
		dcpConfig.addRule(rule);

		// Then
		assertThat(dcpConfig.getRuleMappings()).hasSize(1);
		assertThat(dcpConfig.getRuleMappings()
							.get(0)
							.getRule()).isEqualTo(rule);
	}

	@Test
	@DisplayName("getRules 메서드는 DcpConfig가 가진 Rule 리스트를 반환한다")
	void getRules_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Rule rule1 = new Rule();
		Rule rule2 = new Rule();
		DcpConfigRuleMapping mapping1 = DcpConfigRuleMapping.create(dcpConfig, rule1);
		DcpConfigRuleMapping mapping2 = DcpConfigRuleMapping.create(dcpConfig, rule2);
		dcpConfig.getRuleMappings()
				 .add(mapping1);
		dcpConfig.getRuleMappings()
				 .add(mapping2);

		// When
		List<Rule> rules = dcpConfig.getRules();

		// Then
		assertThat(rules).hasSize(2)
						 .contains(rule1, rule2);
	}

	@Test
	@DisplayName("getRules 메서드는 Rule 객체가 없을 때 빈 리스트를 반환한다")
	void getRules_noRules_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();

		// When
		List<Rule> rules = dcpConfig.getRules();

		// Then
		assertThat(rules).isEmpty();
	}

	@Test
	@DisplayName("modify 메서드는 주어진 데이터로 DcpConfig 객체의 필드를 수정한다")
	void modify_test() {
		// Given
		Tool originalTool = new Tool();
		List<Parameter> originalParameters = List.of(new Parameter());
		List<Rule> originalRules = List.of(new Rule());

		DcpConfig dcpConfig = DcpConfig.create(DcpConfig.builder()
														.topic("originalTopic")
														.bootstrapServer("originalBootstrapServer")
														.command("originalCommand")
														.dataInterval(10)
														.build(), originalTool, originalParameters, originalRules);

		boolean createdIsUse = dcpConfig.getIsUse();

		List<Parameter> modifyParameters = List.of(new Parameter(), new Parameter());
		List<Rule> modifyRules = List.of(new Rule(), new Rule(), new Rule());
		DcpConfig modifyData = DcpConfig.builder()
										.topic("modifiedTopic")
										.bootstrapServer("modifiedBootstrapServer")
										.command("modifiedCommand")
										.dataInterval(20)
										.build();

		// When
		dcpConfig.modify(modifyData, modifyParameters, modifyRules);

		// Then
		assertThat(dcpConfig.getTopic()).isEqualTo("originalTopic");
		assertThat(dcpConfig.getBootstrapServer()).isEqualTo(modifyData.getBootstrapServer());
		assertThat(dcpConfig.getCommand()).isEqualTo(modifyData.getCommand());
		assertThat(dcpConfig.getDataInterval()).isEqualTo(modifyData.getDataInterval());
		assertThat(dcpConfig.getParameters()).hasSize(modifyParameters.size());
		assertThat(dcpConfig.getRules()).hasSize(modifyRules.size());

		// 이건 변경되지 않는다.
		// 차후 스펙이 변경된다면 수정 필요하다
		assertThat(dcpConfig.getIsUse()).isEqualTo(createdIsUse);
	}

	@Test
	@DisplayName("modify 메서드는 modifyData가 null일 때 DcpConfig 객체의 필드를 수정하지 않는다")
	void modify_nullData_test() {
		// Given
		Tool originalTool = new Tool();
		List<Parameter> originalParameters = List.of(new Parameter());
		List<Rule> originalRules = List.of(new Rule());

		DcpConfig dcpConfig = DcpConfig.create(DcpConfig.builder()
														.topic("originalTopic")
														.bootstrapServer("originalBootstrapServer")
														.command("originalCommand")
														.dataInterval(10)
														.build(), originalTool, originalParameters, originalRules);

		DcpConfig originalDcpConfig = DcpConfig.builder()
											   .topic(dcpConfig.getTopic())
											   .bootstrapServer(dcpConfig.getBootstrapServer())
											   .command(dcpConfig.getCommand())
											   .dataInterval(dcpConfig.getDataInterval())
											   .isUse(dcpConfig.getIsUse())
											   .build();

		// When
		dcpConfig.modify(null, originalParameters, originalRules);

		// Then
		assertThat(dcpConfig.getTopic()).isEqualTo("originalTopic");
		assertThat(dcpConfig.getBootstrapServer()).isEqualTo(originalDcpConfig.getBootstrapServer());
		assertThat(dcpConfig.getCommand()).isEqualTo(originalDcpConfig.getCommand());
		assertThat(dcpConfig.getDataInterval()).isEqualTo(originalDcpConfig.getDataInterval());
		assertThat(dcpConfig.getIsUse()).isEqualTo(originalDcpConfig.getIsUse());
		assertThat(dcpConfig.getParameters()).hasSize(originalParameters.size());
		assertThat(dcpConfig.getRules()).hasSize(originalRules.size());
	}
}