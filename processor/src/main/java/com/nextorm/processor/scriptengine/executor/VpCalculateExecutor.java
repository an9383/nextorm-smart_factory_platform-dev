package com.nextorm.processor.scriptengine.executor;

import com.nextorm.processor.scriptengine.BindingMember;

/**
 * 이 인터페이스는 Virtual Parameter Calculate Executor를 나타냅니다.
 * 가상 파라미터를 계산하는 로직을 정의하는 역할을 합니다.
 */
public interface VpCalculateExecutor {
	/**
	 * 스크립트 엔진에 등록할 스크립트를 반환합니다.
	 *
	 * @return: 스크립트 문자열
	 */
	String getScript();

	/**
	 * 스크립트 엔진에 등록할 객체 정보를 반환합니다.
	 *
	 * @return: BindingMember
	 */
	BindingMember getBindingMember();
}
