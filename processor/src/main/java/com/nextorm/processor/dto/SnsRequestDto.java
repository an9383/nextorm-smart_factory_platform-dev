package com.nextorm.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnsRequestDto {
	private String plusFriendId;
	private String templateCode;
	private List<SnsMessage> messages;

	@Builder
	@Data
	public static class SnsMessage {
		private String to;
		private String content;
	}
}
