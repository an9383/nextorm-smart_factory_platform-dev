package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process_config_tool_lnk")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProcessConfigToolMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "process_config_id")
	private ProcessConfig processConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@Builder(access = AccessLevel.PRIVATE)
	public ProcessConfigToolMapping(
		ProcessConfig processConfig,
		Tool tool
	) {
		this.processConfig = processConfig;
		this.tool = tool;
	}

	public static ProcessConfigToolMapping create(
		ProcessConfig processConfig,
		Tool tool
	) {
		return ProcessConfigToolMapping.builder()
									   .processConfig(processConfig)
									   .tool(tool)
									   .build();
	}
}
