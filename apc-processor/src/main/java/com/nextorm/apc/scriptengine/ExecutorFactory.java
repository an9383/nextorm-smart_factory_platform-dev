package com.nextorm.apc.scriptengine;

import com.nextorm.apc.scriptengine.executors.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

@Slf4j
@Component
public class ExecutorFactory {
	private final ApplicationContext context;
	private final List<String> executorClassNames;

	public ExecutorFactory(ApplicationContext context) {
		this.context = context;

		Set<BeanDefinition> beanDefinitions = getVpCalculateExecutorClasses();
		executorClassNames = beanDefinitions.stream()
											.map(BeanDefinition::getBeanClassName)
											.toList();
	}

	private Set<BeanDefinition> getVpCalculateExecutorClasses() {
		Class<?> executorClass = VpCalculateExecutor.class;
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(executorClass));
		return scanner.findCandidateComponents(executorClass.getPackageName());
	}

	public List<VpCalculateExecutor> createExecutor(
		Map<Class<?>, Object> priorityObjects
	) {
		List<VpCalculateExecutor> instances = new ArrayList<>();
		for (String className : executorClassNames) {
			try {
				Constructor<?> constructor = getMaxArgsConstructor(Class.forName(className));
				List<Object> dependencies = getDependencies(constructor, priorityObjects);
				VpCalculateExecutor instance = (VpCalculateExecutor)constructor.newInstance(dependencies.toArray());
				instances.add(instance);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				String message = "Executor 생성에 실패함: " + className;
				throw new RuntimeException(message, e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return instances;
	}

	private Constructor<?> getMaxArgsConstructor(Class<?> clazz) {
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		return Arrays.stream(constructors)
					 .max(Comparator.comparingInt(Constructor::getParameterCount))
					 .orElseThrow(() -> new RuntimeException("No constructor found"));
	}

	private List<Object> getDependencies(
		Constructor<?> constructor,
		Map<Class<?>, Object> priorityObjects
	) {
		List<Object> dependencies = new ArrayList<>();
		for (Parameter parameter : constructor.getParameters()) {
			Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
			if (qualifier != null) {
				String beanName = qualifier.value();
				Object bean = context.getBean(beanName);
				dependencies.add(bean);
				continue;
			}

			Object dependency = priorityObjects.get(parameter.getType());
			if (dependency == null) {
				dependency = context.getBean(parameter.getType());
			}

			dependencies.add(dependency);
		}
		return dependencies;
	}
}
