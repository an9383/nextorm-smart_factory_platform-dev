package com.nextorm.collector.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class OnArgumentCollectorTypeCondition implements Condition {
	@Override
	public boolean matches(
		ConditionContext context,
		AnnotatedTypeMetadata metadata
	) {
		Environment env = context.getEnvironment();

		log.info("type: {}, {}", env.getProperty("collectorType"), env.getProperty("collector"));
		// 어플리케이션의 실행 인자값을 가져옵니다.
		List<String> types = Arrays.asList(env.getProperty("collectorType"), env.getProperty("collector"));

		// 어노테이션의 value 값을 가져옵니다.
		Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnArgumentCollectorType.class.getName());
		String collectorType = (String)attributes.get("collectorType");

		// 인자값을 확인하여 조건을 만족시키는지 확인합니다.
		for (String type : types) {
			if (type == null) {
				continue;
			}
			if (collectorType.equalsIgnoreCase(type)) {
				return true;
			}
		}
		return false;
	}
}
