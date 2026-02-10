package com.nextorm.portal.dto.ocap;

import lombok.Data;

import java.util.List;

@Data
public class RecipientInfo {
	private Long userId;
	private List<String> notificationTypes;
}
