package com.nextorm.processor.scriptengine;

public class UnsupportedConstructorParameterTypeException extends RuntimeException {
	public UnsupportedConstructorParameterTypeException(String className) {
		super("지원되지 않는 생성자 타입을 사용하고 있습니다. 클래스명: " + className);
	}
}
