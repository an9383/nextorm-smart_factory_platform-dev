package com.nextorm.collector.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnArgumentCollectorTypeCondition.class)
public @interface ConditionalOnArgumentCollectorType {
	String collectorType();
}
