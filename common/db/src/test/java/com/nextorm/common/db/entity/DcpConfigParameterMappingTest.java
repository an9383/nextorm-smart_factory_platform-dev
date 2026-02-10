package com.nextorm.common.db.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DcpConfigParameterMappingTest {

	@DisplayName("DcpConfigParameterMapping을 생성한다")
	@Test
	void create_test() {
		// Given
		DcpConfig dcpConfig = new DcpConfig();
		Parameter parameter = new Parameter();

		// When
		DcpConfigParameterMapping mapping = DcpConfigParameterMapping.create(dcpConfig, parameter);

		// Then
		assertThat(mapping).isNotNull();
		assertThat(dcpConfig).isEqualTo(mapping.getDcpConfig());
		assertThat(parameter).isEqualTo(mapping.getParameter());
	}
}