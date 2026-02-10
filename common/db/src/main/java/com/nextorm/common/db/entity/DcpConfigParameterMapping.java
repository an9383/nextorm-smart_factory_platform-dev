package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dcp_config_parameter_lnk")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DcpConfigParameterMapping extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dcp_config_id")
	private DcpConfig dcpConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parameter_id")
	private Parameter parameter;

	public static DcpConfigParameterMapping create(
		DcpConfig dcpConfig,
		Parameter parameter
	) {
		return DcpConfigParameterMapping.builder()
										.dcpConfig(dcpConfig)
										.parameter(parameter)
										.build();
	}
}
