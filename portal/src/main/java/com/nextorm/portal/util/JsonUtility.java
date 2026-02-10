package com.nextorm.portal.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JsonUtility {
	private static final ObjectMapper globalObjectMapper = new ObjectMapper();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private JsonNode mainJsonNode = null;
	private String jsonString = "";

	static {
		globalObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		globalObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		globalObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		globalObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public JsonUtility() {
	}

	// 객체를 JSON String으로 변환
	public static <T> String ObjectToString(T sourceObject) {
		try {
			return globalObjectMapper.writeValueAsString(sourceObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 객체를 포맷된 JSON String으로 변환
	public static <T> String ObjectToFormattedString(T sourceObject) {
		try {
			return globalObjectMapper.writerWithDefaultPrettyPrinter()
									 .writeValueAsString(sourceObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// JSON 파일을 객체로 변환
	public static <T> T fileToObject(
		String filePath,
		Class<T> classType
	) {
		try {
			return globalObjectMapper.readValue(new File(filePath), classType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 객체를 JSON 파일로 저장
	public static <T> void objectToFile(
		String filePath,
		T sourceObject
	) {
		try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
			globalObjectMapper.writeValue(fileOutputStream, sourceObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 객체를 포맷된 JSON 파일로 저장
	public static <T> void objectToFileWithFormatted(
		String filePath,
		T sourceObject
	) {
		try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
			globalObjectMapper.writerWithDefaultPrettyPrinter()
							  .writeValue(fileOutputStream, sourceObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// JSON 문자열을 객체로 변환
	public static <T> T StringToObject(
		String jsonString,
		Class<T> classType
	) {
		try {
			return globalObjectMapper.readValue(jsonString, classType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// JSON 문자열을 타입 참조 객체로 변환
	public static <T> T StringToObject(
		String jsonString,
		TypeReference<T> typeReference
	) {
		try {
			return globalObjectMapper.readValue(jsonString, typeReference);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// JSON 문자열에서 특정 키의 값을 가져옴
	public static String getData(
		String jsonString,
		String itemName,
		String defaultData
	) {
		try {
			JsonNode jsonNode = globalObjectMapper.readTree(jsonString);
			return Optional.ofNullable(jsonNode.get(itemName))
						   .map(JsonNode::asText)
						   .orElse(defaultData);
		} catch (IOException e) {
			return defaultData;
		}
	}

	// JSON 문자열에서 특정 키의 값을 Set으로 반환
	public static Set<String> getStringArray(
		String jsonString,
		String arrayName
	) {
		Set<String> result = new HashSet<>();
		try {
			JsonNode node = globalObjectMapper.readTree(jsonString)
											  .get(arrayName);
			if (node != null && node.isArray()) {
				for (JsonNode jsonNode : node) {
					result.add(jsonNode.asText());
				}
			}
		} catch (IOException ignored) {
		}
		return result;
	}

	// JSON 문자열을 객체로 변환
	public <T> T getStringToObject(
		String jsonString,
		Class<T> classType
	) {
		try {
			return objectMapper.readValue(jsonString, classType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// JSON 문자열을 HashMap으로 변환
	public <T> Map<String, T> getHashMapFromString(String jsonString) {
		try {
			return objectMapper.readValue(jsonString, new TypeReference<Map<String, T>>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
