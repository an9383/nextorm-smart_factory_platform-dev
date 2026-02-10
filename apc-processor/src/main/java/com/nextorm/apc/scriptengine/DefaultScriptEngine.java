package com.nextorm.apc.scriptengine;

import com.nextorm.apc.exception.FormulaExecutionException;
import com.nextorm.apc.scriptengine.executors.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultScriptEngine implements ScriptEngine {
	private final Context context;
	private final Value value;

	public DefaultScriptEngine(
		String initScript,
		List<VpCalculateExecutor> executors
	) {
		context = Context.newBuilder("js")
						 .allowAllAccess(true)
						 .build();

		this.context.eval("js", """
				var globalArguments = {};
				var globalMetaData = {};
			
				var getMetaDataValue = function(key) {
					return globalMetaData.get(key);
				};
				var getSourceValue = function(key) {};
			
				function isSimulation() {
					return getMetaDataValue('isSimulation');
				}
			""");

		for (VpCalculateExecutor executor : executors) {
			BindingMember bindingMember = executor.getBindingMember();
			this.context.getBindings("js")
						.putMember(bindingMember.getKey(), bindingMember.getValue());
			this.context.eval("js", executor.getScript());
		}

		String mainScriptFormat = """
				function execute(arguments) {
					globalArguments = arguments;
					const source = arguments.source();
					const metaData = arguments.metaData();
			
					globalMetaData = metaData;
					getSourceValue = function(key) {
						const value = source.getOrDefault(key, null);
						if (value) {
							return parseFloat(value, 10);
						}
						return value;
					};
			
					%s
				}
				execute;
			""";

		try {
			this.value = this.context.eval("js", mainScriptFormat.formatted(initScript));
		} catch (RuntimeException e) {
			throw new FormulaExecutionException(e.getCause());
		}

		if (!this.value.canExecute()) {
			throw new FormulaExecutionException("스크립트 엔진이 실행 가능한 상태가 아닙니다. 스크립트를 확인해주세요.");
		}

	}

	@Override
	public EngineExecuteResult execute(ExecuteArguments arguments) {
		log.info("DefaultScriptEngine execute. argumentMap: {}", arguments);
		try {
			Value execute = value.execute(arguments);
			return EngineExecuteResult.success(execute.as(Map.class));
		} catch (Exception e) {
			throw new FormulaExecutionException(e);
		}
	}
}
