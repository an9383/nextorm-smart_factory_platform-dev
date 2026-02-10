package com.nextorm.processor.scriptengine;

import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ScriptEngineFactory {
	private final ApplicationContext context;

	private List<Class<?>> vpExecutorClasses;

	@PostConstruct
	public void init() {
		Set<BeanDefinition> beanDefinitions = getVpCalculateExecutorClasses();
		this.vpExecutorClasses = beanDefinitions.stream()
												.<Class<?>>map(bd -> {
													try {
														return Class.forName(bd.getBeanClassName());
													} catch (ClassNotFoundException e) {
														throw new RuntimeException("클래스를 찾을 수 없습니다: " + bd.getBeanClassName(),
															e);
													}
												})
												.toList();
	}

	private Set<BeanDefinition> getVpCalculateExecutorClasses() {
		Class<?> executorClass = VpCalculateExecutor.class;
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(executorClass));
		return scanner.findCandidateComponents(executorClass.getPackageName());
	}

	public ScriptEngine createEngine(
		String virtualParameterScript,
		ParameterContainer parameterContainer
	) {
		Map<Class<?>, Object> priorityObjects = new HashMap<>();
		priorityObjects.put(ParameterContainer.class, parameterContainer);
		return createEngine(virtualParameterScript, priorityObjects);
	}

	public ScriptEngine createEngine(
		String virtualParameterScript,
		Map<Class<?>, Object> priorityObjects
	) {
		ScriptEngine engine = new ScriptEngine(virtualParameterScript);

		// ExecuteContext 주입
		if (!priorityObjects.containsKey(ExecuteContext.class)) {
			priorityObjects.put(ExecuteContext.class, engine);
		}

		List<VpCalculateExecutor> executors = createExecutors(priorityObjects);
		engine.addExecutors(executors);
		if (!engine.eval()) {
			throw new ScriptInitFailException();
		}

		return engine;
	}

	private List<VpCalculateExecutor> createExecutors(Map<Class<?>, Object> priorityObjects) {
		return vpExecutorClasses.stream()
								.<VpCalculateExecutor>map(clazz -> createExecutorInstance(clazz, priorityObjects))
								.toList();
	}

	private <T> T createExecutorInstance(
		Class<?> clazz,
		Map<Class<?>, Object> priorityObjects
	) {
		Constructor<?> targetConstructor = findConstructorWithMostParameters(clazz);
		List<Object> args = getDependencies(targetConstructor, priorityObjects);

		try {
			return (T)targetConstructor.newInstance(args.toArray());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
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
				dependencies.add(context.getBean(beanName));
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

	/**
	 * 주어진 클래스에서 매개변수의 개수가 가장 많은 생성자를 찾습니다.
	 *
	 * @param clazz 생성자를 검색할 대상 클래스
	 * @return 매개변수의 개수가 가장 많은 생성자
	 * @throws RuntimeException 생성자가 존재하지 않을 경우 예외를 발생시킴
	 */
	private Constructor<?> findConstructorWithMostParameters(Class<?> clazz) {
		Constructor<?>[] constructors = clazz.getConstructors();
		return Arrays.stream(constructors)
					 .max(Comparator.comparingInt(Constructor::getParameterCount))
					 .orElseThrow(() -> new RuntimeException("생성자가 존재하지 않습니다. 클래스명: " + clazz.getName()));
	}
}
