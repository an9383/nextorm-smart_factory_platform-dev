package com.nextorm.common.db.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {
	private final String key;
	private final String message;
}
