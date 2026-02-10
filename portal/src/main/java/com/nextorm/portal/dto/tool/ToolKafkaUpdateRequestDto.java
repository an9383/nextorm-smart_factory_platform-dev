package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.ToolKafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ToolKafkaUpdateRequestDto {
	private String bootstrapServer;

	public ToolKafka toEntity() {
		return ToolKafka.builder()
						.bootstrapServer(bootstrapServer)
						.build();
	}
}
