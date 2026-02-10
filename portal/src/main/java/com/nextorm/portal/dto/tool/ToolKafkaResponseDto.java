package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.ToolKafka;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ToolKafkaResponseDto {
	private Long toolId;
	private String bootstrapServer;
	private String topic;

	public static ToolKafkaResponseDto from(ToolKafka toolKafka) {
		if (toolKafka == null) {
			return null;
		}
		return ToolKafkaResponseDto.builder()
								   .toolId(toolKafka.getToolId())
								   .bootstrapServer(toolKafka.getBootstrapServer())
								   .topic(toolKafka.getTopic())
								   .build();
	}
}
