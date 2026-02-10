package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dcp_config_rule_lnk")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DcpConfigRuleMapping extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dcp_config_id")
	private DcpConfig dcpConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rule_id")
	private Rule rule;

	public static DcpConfigRuleMapping create(
		DcpConfig dcpConfig,
		Rule rule
	) {
		return DcpConfigRuleMapping.builder()
								   .dcpConfig(dcpConfig)
								   .rule(rule)
								   .build();
	}
}
