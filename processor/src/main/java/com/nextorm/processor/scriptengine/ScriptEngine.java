package com.nextorm.processor.scriptengine;

import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ScriptEngine implements ExecuteContext {
	public static final String CONTEXT_CONTAINER_NAME = "contextContainer";
	public static final String EXECUTION_PARAMETER_ID_NAME = "executionParameterId";
	public static final String PREVIOUS_CONTEXT_CONTAINER_NAME = "previousContextContainer";

	private static final String CONTEXT_CONTAINER_SCRIPT = """
		var %s = {};
		var %s = null;
		var %s = {};
		""".formatted(CONTEXT_CONTAINER_NAME, EXECUTION_PARAMETER_ID_NAME, PREVIOUS_CONTEXT_CONTAINER_NAME);

	private final String executeScript;
	private final Context context;
	private Value value;

	// executeContext 관련 변수
	private Long executionParameterId;
	private Map<Long, Object> paramIdValueMap = new HashMap<>();
	private Map<Long, Object> previousParamIdValueMap = new HashMap<>();

	public ScriptEngine(String script) {
		context = Context.newBuilder("js")
						 .allowAllAccess(true)
						 .build();

		// MAIN 메소드 역할을 하는 함수 스크립트

		this.executeScript = """
			function setExecutionParameterId(paramId) {
				executionParameterId = paramId;
			}
			
			function setValuesToContextContainer(paramIdValueMap) {
				contextContainer = {};
				if (paramIdValueMap == null) {
					return;
				}
			    paramIdValueMap.entrySet().forEach(entry => {
					contextContainer[entry.getKey()] = entry.getValue()
				});
			}
			
			function setPreviousValuesToContextContainer(previousParamIdValueMap) {
				previousContextContainer = {};
				if (previousParamIdValueMap == null) {
					return;
				}
			    previousParamIdValueMap.entrySet().forEach(entry => {
					previousContextContainer[entry.getKey()] = entry.getValue()
				});
			}
			
			function getObjectValueByParameter(parameterId) {
				const found = %s[parameterId];
				if (found == undefined) {
					return null;
				}
				return found;
			}
			
			function execute(source) {
				const { executionParameterId: executionId, paramIdValueMap, previousParamIdValueMap } = source;
				setExecutionParameterId(executionId);
			    setValuesToContextContainer(paramIdValueMap);
				setPreviousValuesToContextContainer(previousParamIdValueMap);
			    %s
			}
			
			execute;
			""".formatted(CONTEXT_CONTAINER_NAME, script);
		this.context.eval("js", CONTEXT_CONTAINER_SCRIPT);
	}

	/**
	 * @param executionParameterId: 실행하는 가상 파라미터 아이디
	 * @param paramIdValueMap:      실행함수에 전달할 파라미터
	 * @return: 실행함수의 반환값
	 */
	public Object execute(
		Long executionParameterId,
		Map<Long, Object> paramIdValueMap,
		Map<Long, Object> previousParamIdValueMap
	) {
		if (value == null) {
			eval();
		}

		this.executionParameterId = executionParameterId;
		this.paramIdValueMap = paramIdValueMap;
		this.previousParamIdValueMap = previousParamIdValueMap;

		HashMap<String, Object> source = new HashMap<>();
		source.put("executionParameterId", executionParameterId);
		source.put("paramIdValueMap", paramIdValueMap);
		source.put("previousParamIdValueMap", previousParamIdValueMap);

		return value.execute(source)
					.as(Object.class);
	}

	/**
	 * 스크립트 엔진에 executor로부터 받은 정보를 등록한다
	 *
	 * @param executors: 엔진에 등록할 데이터를 담고 있는 객체 목록
	 */
	public void addExecutors(List<VpCalculateExecutor> executors) {
		for (VpCalculateExecutor executor : executors) {
			BindingMember bindingMember = executor.getBindingMember();
			if (bindingMember != null) {
				context.getBindings("js")
					   .putMember(bindingMember.getKey(), bindingMember.getValue());
			}
			context.eval("js", executor.getScript());
			log.info("add Script: {}", executor.getScript());
		}
	}

	/**
	 * 스크립트 엔진에 등록된 스크립트를 평가한다.
	 *
	 * @return: 스크립트 엔진에 등록된 스크립트 평가 결과 true: 평가 성공, false: 평가 실패
	 */
	public boolean eval() {
		value = context.eval("js", executeScript);
		return value.canExecute();
	}

	public void close() {
		context.close();
	}

	@Override
	public Long getExecutionParameterId() {
		return this.executionParameterId;
	}

	@Override
	public Map<Long, Object> getParamIdValueMap() {
		return this.paramIdValueMap;
	}

	@Override
	public Map<Long, Object> getPreviousParamIdValueMap() {
		return this.previousParamIdValueMap;
	}
}
