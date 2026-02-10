package com.nextorm.apc.scriptengine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스크립트 엔진에 바인딩할 멤버를 담는 클래스
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BindingMember {
	private final String key;
	private final Object value;

	public static BindingMember create(
		String key,
		Object value
	) {
		return new BindingMember(key, value);
	}
}
