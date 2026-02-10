package com.nextorm.portal.ai.openai.restapi.dto;

import com.nextorm.portal.ai.openai.constant.Status;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RunResponseDto {
	private String id;
	private Long createdAt;
	private String threadId;
	private String assistantId;
	private Status status;
	private RequiredAction requiredAction;

	@Data
	public static class RequiredAction {
		private String type;
		private SubmitToolOutputs submitToolOutputs;
	}

	@Data
	public static class SubmitToolOutputs {
		private List<ToolCall> toolCalls;

	}

	@Data
	public static class ToolCall {
		private String id;
		private String type;
		private ToolCall.Function function;

		@Data
		public static class Function {
			private String name;
			private Map<String, Object> arguments = new HashMap<>();
		}
	}
}
