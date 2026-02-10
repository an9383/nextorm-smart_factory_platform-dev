package com.nextorm.processor.scriptengine;

public class ScriptInitFailException extends RuntimeException {
	private static final String MESSAGE = "script engine에서 script eval중 문제 발생";

	public ScriptInitFailException() {
		super(MESSAGE);
	}
}
