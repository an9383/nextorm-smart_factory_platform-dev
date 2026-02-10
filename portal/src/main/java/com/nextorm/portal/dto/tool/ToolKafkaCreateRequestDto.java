package com.nextorm.portal.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolKafkaCreateRequestDto {
	private String bootstrapServer;
}
