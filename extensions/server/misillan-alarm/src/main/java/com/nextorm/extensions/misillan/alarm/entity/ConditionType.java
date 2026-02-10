package com.nextorm.extensions.misillan.alarm.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ConditionType {
	TEMPERATURE("온도"), PRESSURE("압력"), METAL_DETECT("금속검출");
		private final String displayName;
}
