package com.nextorm.processor.dto;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequestDto {
	private String type = "SMS";
	private String from;
	private String subject;
	private String content;
	private List<smsRecipients> messages;


	@Data
	@Builder
	public static class smsRecipients {
		private String to;
	}

}
