package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "summary_config_tool_lnk")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SummaryConfigToolMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "summary_config_id")
	private SummaryConfig summaryConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@Builder(access = AccessLevel.PRIVATE)
	public SummaryConfigToolMapping(
		SummaryConfig summaryConfig,
		Tool tool
	) {
		this.summaryConfig = summaryConfig;
		this.tool = tool;
	}

	public static SummaryConfigToolMapping create(
		SummaryConfig summaryConfig,
		Tool tool
	) {
		return SummaryConfigToolMapping.builder()
									   .summaryConfig(summaryConfig)
									   .tool(tool)
									   .build();
	}
}
