package com.nextorm.common.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tool_kafka")
public class ToolKafka extends BaseEntity {

	@Column(name = "tool_id")
	private Long toolId;

	@Column(name = "bootstrap_server")
	private String bootstrapServer;

	@Column(name = "topic")
	private String topic;

	public static ToolKafka create(
		Long toolId,
		String bootstrapServer,
		String topic
	) {
		return ToolKafka.builder()
						.toolId(toolId)
						.bootstrapServer(bootstrapServer)
						.topic(topic)
						.build();
	}

	public void modify(
		String bootstrapServer
	) {
		this.bootstrapServer = bootstrapServer;
	}
}
