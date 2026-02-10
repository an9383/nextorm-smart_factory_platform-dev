package com.nextorm.collector.collector.opcuacollector.data;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtility {
	private static ObjectMapper globalObjectMapper = new ObjectMapper();

	public static <T> String ObjectToFormattedString(T p_sourceObject) throws RuntimeException {
		String json = "";
		try {
			json = globalObjectMapper.writerWithDefaultPrettyPrinter()
									 .writeValueAsString(p_sourceObject);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return json;
	}
}
