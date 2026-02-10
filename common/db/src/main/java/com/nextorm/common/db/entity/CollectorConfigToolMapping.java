package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "collector_config_tool_lnk")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CollectorConfigToolMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collector_config_id")
	private CollectorConfig collectorConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@Builder(access = AccessLevel.PRIVATE)
	public CollectorConfigToolMapping(
		CollectorConfig collectorConfig,
		Tool tool
	) {
		this.collectorConfig = collectorConfig;
		this.tool = tool;
	}

	public static CollectorConfigToolMapping create(
		CollectorConfig collectorConfig,
		Tool tool
	) {
		return CollectorConfigToolMapping.builder()
										 .collectorConfig(collectorConfig)
										 .tool(tool)
										 .build();
	}
}
