package com.nextorm.processor.scriptengine;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ScriptEngineTest {

	@DisplayName("엔진에 등록한 스크립트 실행 결과를 반환한다 (단순 반환)")
	@Test
	void execute_engine_simple_return() {
		String script = "return 10.123499;";
		ScriptEngine engine = new ScriptEngine(script);
		Object result = engine.execute(null, null, null);
		double parseResult = Double.parseDouble(result.toString());
		log.info("parseResult: {}", result);
		assertThat(parseResult).isEqualTo(10.123499d);
	}

	@DisplayName("엔진에 등록한 스크립트 실행 결과를 반환한다 (로직 포함)")
	@Test
	void execute_engine_with_logic() {
		String script = """
			const a = 10;
			let result = 0;
			for (let i = 0; i < a; i++) {
				if (i % 2 === 0) {
					result += i;
				}
			}
			return result;			
			""";
		ScriptEngine engine = new ScriptEngine(script);
		Object result = engine.execute(null, null, null);
		double parseResult = Double.parseDouble(result.toString());
		log.info("parseResult: {}", parseResult);
		assertThat(parseResult).isEqualTo(20d);
	}

	@DisplayName("엔진 실행할 때 넘긴 Map의 값을, 스크립트 실행 도중에 참조한다")
	@Test
	void execute_engine_get_value_by_paramMap() {
		String script = """
			const container = %s;
			return container['1'];
			""".formatted(ScriptEngine.CONTEXT_CONTAINER_NAME);
		ScriptEngine engine = new ScriptEngine(script);
		Object result = engine.execute(null, Map.of(1L, "ONE"), null);
		String parseResult = result.toString();
		log.info("parseResult: {}", parseResult);
		assertThat(parseResult).isEqualTo("ONE");
	}
}
