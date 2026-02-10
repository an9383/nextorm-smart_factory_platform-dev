package com.nextorm.collector.collector;

import com.nextorm.common.define.collector.ArgumentType;
import com.nextorm.common.define.collector.CollectorArgument;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class CollectorFactory {
	private final ApplicationContext context;

	public Collector createCollector(
		CollectorType collectorType,
		DataCollectPlan collectPlan,
		Map<Class<?>, Object> priorityObjects
	) {
		validateArguments(collectorType, collectPlan);
		Constructor<?> constructor = getMaxArgsConstructor(collectorType.getCollectorClass());
		List<Object> dependencies = getDependencies(constructor, collectPlan, priorityObjects);

		try {
			return (Collector)constructor.newInstance(dependencies.toArray());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			String message = "컬렉터 생성에 실패함. 타입: " + collectorType + ", DcpID: " + collectPlan.getDcpId();
			throw new RuntimeException(message, e);
		}
	}

	private void validateArguments(
		CollectorType collectorType,
		DataCollectPlan collectPlan
	) {
		final Set<String> configArgumentsKeys = collectPlan.getCollectorArguments()
														   .keySet();

		/*
			TODO:: PARAMETER_MAPPING 인경우에 필요한 값이 모두 존재하는지 검증을 보강할 필요가 있다.
		 */
		boolean isAllContainsArguments = collectorType.getArguments()
													  .stream()
													  .filter(argument -> argument.getType() != ArgumentType.PARAMETER_MAPPING)
													  .allMatch(argument -> configArgumentsKeys.contains(argument.getKey()));

		if (!isAllContainsArguments) {
			List<String> collectorRequiredArgumentKeys = collectorType.getArguments()
																	  .stream()
																	  .map(CollectorArgument::getKey)
																	  .toList();

			throw new IllegalArgumentException("컬렉터에 필요한 모든 인자가 설정되지 않음. DcpId: " + collectPlan.getDcpId() + ", 필요인자: " + collectorRequiredArgumentKeys);
		}
	}

	private Constructor<?> getMaxArgsConstructor(Class<?> clazz) {
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		return Arrays.stream(constructors)
					 .max(Comparator.comparingInt(Constructor::getParameterCount))
					 .orElseThrow(() -> new RuntimeException("No constructor found"));
	}

	private List<Object> getDependencies(
		Constructor<?> constructor,
		DataCollectPlan collectPlan,
		Map<Class<?>, Object> priorityObjects
	) {
		List<Object> dependencies = new ArrayList<>();
		for (Parameter parameter : constructor.getParameters()) {
			if (parameter.getType()
						 .equals(DataCollectPlan.class)) {
				dependencies.add(collectPlan);
				continue;
			}

			Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
			if (qualifier != null) {
				String beanName = qualifier.value();
				Object bean = context.getBean(beanName);
				if (bean == null) {
					throw new RuntimeException("Named Bean not found: " + beanName);
				}
				dependencies.add(bean);
				continue;
			}

			Object dependency = priorityObjects.get(parameter.getType());
			if (dependency == null) {
				dependency = context.getBean(parameter.getType());
			}

			if (dependency == null) {
				throw new RuntimeException("Dependency not found: " + parameter.getType());
			}

			dependencies.add(dependency);
		}
		return dependencies;
	}
}
