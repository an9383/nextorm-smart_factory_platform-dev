package com.nextorm.common.db.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OcapAlarmNotificationType {
	EMAIL("이메일"), SMS("문자"), KAKAO("SNS 채널");

	private final String name;
}
