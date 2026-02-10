package com.nextorm.portal.ai.openai.restapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ToolOutputRequestDto {
	private List<ToolOutput> toolOutputs;

	@Data
	@Builder
	public static class ToolOutput {
		private String toolCallId;
		private String output;
	}
}
