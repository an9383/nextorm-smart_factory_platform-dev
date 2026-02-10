package com.nextorm.portal.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponseDto {
	private String threadId;
	private String message;
	private List<ExtraData> extraDatas = new ArrayList<>();

	@Data
	public static class ExtraData {
		public static enum Type {
			LINE_CHART
		}

		private Type type;
		private Map<String, Object> data;
	}
}
