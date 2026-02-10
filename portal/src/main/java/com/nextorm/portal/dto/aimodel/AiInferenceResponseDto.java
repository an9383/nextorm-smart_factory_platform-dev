package com.nextorm.portal.dto.aimodel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AiInferenceResponseDto {
	private Long parameterId;
	private String parameterName;
	private List<Item> items;

	@Data
	@AllArgsConstructor
	public static class Item {
		private LocalDateTime time;
		private Number value;
		private Number originalValue;
	}
}
