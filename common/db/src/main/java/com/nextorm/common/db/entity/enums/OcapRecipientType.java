package com.nextorm.common.db.entity.enums;

public enum OcapRecipientType {
	TO("수신인"), CC("참조"), BCC("숨은 참조");

	private final String name;

	OcapRecipientType(String name) {
		this.name = name;
	}
}
