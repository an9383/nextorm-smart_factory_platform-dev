package com.nextorm.common.db.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DcpConfigRuleMappingTest {
	@DisplayName("DcpConfigRuleMapping 객체를 생성한다")
	@Test
	void create_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Rule rule = new Rule();

		// When
		DcpConfigRuleMapping mapping = DcpConfigRuleMapping.create(dcpConfig, rule);

		// Then
		assertThat(mapping).isNotNull();
		assertThat(mapping.getDcpConfig()).isEqualTo(dcpConfig);
		assertThat(mapping.getRule()).isEqualTo(rule);
	}
}