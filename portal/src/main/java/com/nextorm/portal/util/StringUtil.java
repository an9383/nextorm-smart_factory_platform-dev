package com.nextorm.portal.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StringUtil {

	public static String getThrowableStackTraceAsString(
		Throwable throwable
	) {
		return Arrays.stream(throwable.getStackTrace())
					 .map(StackTraceElement::toString)
					 .reduce(throwable.toString(), (acc, element) -> acc + "\n\tat " + element);
	}
}
