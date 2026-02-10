package com.nextorm.portal.client.inference;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InferenceRangeResponse {
	private boolean success;
	private String message;
	private InferenceData data;

	public static InferenceRangeResponse failure(String message) {
		InferenceRangeResponse response = new InferenceRangeResponse();
		response.setSuccess(false);
		response.setMessage(message);
		return response;
	}

	@Data
	public static class InferenceData {
		private Long parameterId;
		private List<Item> items;
	}

	@Data
	public static class Item {
		private LocalDateTime time;
		private Number value;
		private Number originalValue;
	}
}
