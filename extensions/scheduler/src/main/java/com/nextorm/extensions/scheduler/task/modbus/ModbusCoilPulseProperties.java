package com.nextorm.extensions.scheduler.task.modbus;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class ModbusCoilPulseProperties {
	private final String host;
	private final int port;
	private final int unitId;
	private final List<Integer> addresses;

	public ModbusCoilPulseProperties(Map<String, Object> properties) {
		this.host = parseHostProperty(properties);
		this.port = parsePortProperty(properties);
		this.unitId = parseUnitIdProperty(properties);
		this.addresses = parseAddressesProperty(properties);
	}

	private String parseHostProperty(Map<String, Object> properties) {
		String errorMessagePrefix = "host 프로퍼티 파싱중 에러: ";
		Object value = properties.get("host");
		if (value == null) {
			throw new IllegalArgumentException(errorMessagePrefix + "null 값은 허용되지 않습니다");
		}
		if (!(value instanceof String strValue)) {
			throw new IllegalArgumentException(errorMessagePrefix + "값은 문자열 타입이어야 합니다");
		}
		if (!StringUtils.hasText(strValue)) {
			throw new IllegalArgumentException(errorMessagePrefix + "값이 비어있습니다");
		}
		return strValue;
	}

	private int parsePortProperty(Map<String, Object> properties) {
		Object value = properties.get("port");
		try {
			return parseInt(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("port 프로퍼티 파싱중 에러: " + e.getMessage(), e);
		}
	}

	private int parseInt(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("null 값은 허용되지 않습니다");
		}

		boolean isNotInt = !(value instanceof Integer);
		boolean isNotString = !(value instanceof String);
		if (isNotInt && isNotString) {
			throw new IllegalArgumentException("유효하지 않은 타입 입니다");
		}

		try {
			return Integer.parseInt(value.toString()
										 .trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("유효하지 않은 값 입니다", e);
		}
	}

	private int parseUnitIdProperty(Map<String, Object> properties) {
		Object value = properties.get("unitId");
		try {
			return parseInt(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("unitId 프로퍼티 파싱중 에러: " + e.getMessage(), e);
		}
	}

	private List<Integer> parseAddressesProperty(Map<String, Object> properties) {
		String errorMessagePrefix = "addresses 프로퍼티 파싱중 에러: ";
		Object value = properties.get("addresses");
		if (value == null) {
			throw new IllegalArgumentException(errorMessagePrefix + "null 값은 허용되지 않습니다");
		}

		boolean isNotInt = !(value instanceof Integer);
		boolean isNotString = !(value instanceof String);
		if (isNotInt && isNotString) {
			throw new IllegalArgumentException(errorMessagePrefix + "유효하지 않은 타입 입니다. (쉼표로 구분된 정수 목록이어야 합니다)");
		}

		String addressesStr = value.toString();
		List<Integer> parsedAddresses;

		try {
			parsedAddresses = Arrays.stream(addressesStr.split(","))
									.map(String::trim)
									.filter(s -> !s.isEmpty())
									.map(Integer::parseInt)
									.toList();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(errorMessagePrefix + "유효하지 않은 정수 값이 포함되어 있습니다", e);
		}

		if (parsedAddresses.isEmpty()) {
			throw new IllegalArgumentException(errorMessagePrefix + "주소값이 비어 있습니다. 최소 하나 이상의 주소가 필요합니다");
		}

		return parsedAddresses;
	}
}
