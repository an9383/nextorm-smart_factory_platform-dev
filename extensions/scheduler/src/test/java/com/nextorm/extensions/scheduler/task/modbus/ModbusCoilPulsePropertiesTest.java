package com.nextorm.extensions.scheduler.task.modbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModbusCoilPulsePropertiesTest {

	// 테스트를 위한 유효한 프로퍼티 맵
	private Map<String, Object> validProperties;

	@BeforeEach
	void setUp() {
		validProperties = new HashMap<>();
		validProperties.put("host", "localhost");
		validProperties.put("port", 502);
		validProperties.put("unitId", 1);
		validProperties.put("addresses", "1,2,3");
	}

	@ParameterizedTest
	@MethodSource("validProperties")
	@DisplayName("유효한 프로퍼티로 객체 생성 시 성공")
	void createTaskWithValidProperties(
		Map<String, Object> source,
		ExpectedProperties expected
	) {
		// Given & When
		ModbusCoilPulseProperties properties = new ModbusCoilPulseProperties(source);

		// Then
		assertThat(properties.getHost()).isEqualTo(expected.host());
		assertThat(properties.getPort()).isEqualTo(expected.port());
		assertThat(properties.getUnitId()).isEqualTo(expected.unitId());
		assertThat(properties.getAddresses()).containsExactlyInAnyOrderElementsOf(expected.addresses());
	}

	static Stream<Arguments> validProperties() {
		String host = "localhost";
		int port = 502;
		int unitId = 1;

		Map<String, Object> properties = new HashMap<>();
		properties.put("host", host);
		properties.put("port", port);
		properties.put("unitId", unitId);
		properties.put("addresses", "1,2,3");

		Map<String, Object> prop1 = new HashMap<>(properties);
		ExpectedProperties expectedProperties1 = new ExpectedProperties(host, port, unitId, List.of(1, 2, 3));

		Map<String, Object> prop2 = new HashMap<>(properties);
		prop2.put("addresses", "1");
		ExpectedProperties expectedProperties2 = new ExpectedProperties(host, port, unitId, List.of(1));

		Map<String, Object> prop3 = new HashMap<>(properties);
		prop3.put("addresses", 1);
		ExpectedProperties expectedProperties3 = new ExpectedProperties(host, port, unitId, List.of(1));

		return Stream.of(Arguments.of(prop1, expectedProperties1),
			Arguments.of(prop2, expectedProperties2),
			Arguments.of(prop3, expectedProperties3));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("host 프로퍼티가 유효하지 않을 때 에러 발생")
	void createTaskWithInvalidHost(String host) {
		Map<String, Object> source = new HashMap<>(validProperties);
		source.put("host", host);

		assertThrows(IllegalArgumentException.class, () -> new ModbusCoilPulseProperties(source));
	}

	@ParameterizedTest
	@MethodSource("invalidIntValues")
	@DisplayName("port 프로퍼티가 유효하지 않을 때 에러 발생")
	void createTaskWithInvalidPort(Object port) {
		Map<String, Object> source = new HashMap<>(validProperties);
		source.put("port", port);

		assertThrows(IllegalArgumentException.class, () -> new ModbusCoilPulseProperties(source));
	}

	private static Object[] invalidIntValues() {
		return new Object[] {null, "", "asdf"};
	}

	@ParameterizedTest
	@MethodSource("invalidIntValues")
	@DisplayName("unitId 프로퍼티가 유효하지 않을 때 에러 발생")
	void createTaskWithInvalidUnitId(Object unitId) {
		Map<String, Object> source = new HashMap<>(validProperties);
		source.put("unitId", unitId);

		assertThrows(IllegalArgumentException.class, () -> new ModbusCoilPulseProperties(source));
	}

	@ParameterizedTest
	@MethodSource("invalidAddressesValues")
	@DisplayName("addresses 프로퍼티가 유효하지 않을 때 에러 발생")
	void createTaskWithInvalidAddresses(Object addresses) {
		Map<String, Object> source = new HashMap<>(validProperties);
		source.put("addresses", addresses);

		assertThrows(IllegalArgumentException.class, () -> new ModbusCoilPulseProperties(source));
	}

	private static Object[] invalidAddressesValues() {
		return new Object[] {null, "", "1,a,2"};
	}

	/**
	 * 이 레코드는 테스트에서 기대하는 프로퍼티 값을 저장하기 위한 용도로 사용됩니다.
	 * 실제 테스트에서는 이 레코드를 사용하여 생성된 객체의 프로퍼티와 비교할 수 있습니다.
	 */
	record ExpectedProperties(String host, int port, int unitId, List<Integer> addresses) {
	}
}
