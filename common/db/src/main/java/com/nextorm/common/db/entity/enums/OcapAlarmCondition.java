package com.nextorm.common.db.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OcapAlarmCondition {
	CONTROL_SPEC_OVER("컨트롤 스펙 오버"), SPEC_OVER("스펙 오버");

	private final String name;
}
