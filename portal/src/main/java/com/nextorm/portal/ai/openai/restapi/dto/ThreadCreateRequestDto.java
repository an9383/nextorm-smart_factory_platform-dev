package com.nextorm.portal.ai.openai.restapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class ThreadCreateRequestDto {

	@Data
	@Builder
	static class Thread {
		private List<Message> messages;
	}

	@Data
	@Builder
	static class Message {
		final String role = "user"; //보내는 메시지의 role은 "user"로 고정
		String content;
	}

	private String assistantId;
	private Thread thread;

	public static ThreadCreateRequestDtoBuilder builder(
		String assistantId,
		String message
	) {
		return new ThreadCreateRequestDtoBuilder().assistantId(assistantId)
												  .thread(Thread.builder()
																.messages(Collections.singletonList(Message.builder()
																										   .content(
																											   message)
																										   .build()))
																.build());
	}
}
