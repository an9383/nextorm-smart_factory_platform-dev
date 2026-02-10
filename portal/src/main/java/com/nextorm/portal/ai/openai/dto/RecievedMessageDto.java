package com.nextorm.portal.ai.openai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecievedMessageDto {
	@Data
	@Builder
	public static class ExtraData {
		public static enum Type {
			LINE_CHART
		}

		private ExtraData.Type type;
		private Object data;
	}

	@Data
	@Builder
	public static class Message {
		private String id;
		private String message;
	}

	private String threadId;
	private List<Message> messages;
	private List<ExtraData> extraDatas;

}
