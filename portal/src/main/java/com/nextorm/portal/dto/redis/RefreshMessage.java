package com.nextorm.portal.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshMessage {
	private static final long serialVersionUID = 1L;

	private String toolId;
	private String sender;
	private String topic;
}
