package com.nextorm.portal.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtility {
	public GZipUtility() {
	}

	// 문자열을 압축하여 바이트 배열로 반환
	public static byte[] zipStringToBytes(String input) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
			gzipOutputStream.write(input.getBytes(StandardCharsets.UTF_8));
			gzipOutputStream.finish();
			return byteArrayOutputStream.toByteArray();
		}
	}

	// JSON 객체를 압축하여 바이트 배열로 반환
	public static byte[] zipJsonObjectToBytes(Object jsonObject) throws IOException {
		String objectString = JsonUtility.ObjectToString(jsonObject);
		if (objectString != null) {
			return zipStringToBytes(objectString);
		} else {
			throw new RuntimeException("Cannot convert the requested object to JSON.");
		}
	}

	// 압축된 바이트 배열을 JSON 객체로 변환
	public static <T> T unzipByteToJsonObject(
		byte[] byteData,
		Class<T> classType
	) throws IOException {
		Objects.requireNonNull(byteData, "Input byte array must not be null.");
		String jsonString = unzipStringFromBytes(byteData);
		return JsonUtility.StringToObject(jsonString, classType);
	}

	// 압축된 바이트 배열을 TypeReference를 사용해 JSON 객체로 변환
	public static <T> T unzipByteToJsonObject(
		byte[] byteData,
		TypeReference<T> typeReference
	) throws IOException {
		Objects.requireNonNull(byteData, "Input byte array must not be null.");
		String jsonString = unzipStringFromBytes(byteData);
		return JsonUtility.StringToObject(jsonString, typeReference);
	}

	// 바이트 배열을 압축
	public static byte[] zipByteToBytes(byte[] sourceData) throws IOException {
		Objects.requireNonNull(sourceData, "Input byte array must not be null.");
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
			gzipOutputStream.write(sourceData);
			gzipOutputStream.finish();
			return byteArrayOutputStream.toByteArray();
		}
	}

	// 압축된 바이트 배열을 문자열로 변환
	public static String unzipStringFromBytes(byte[] bytes) throws IOException {
		Objects.requireNonNull(bytes, "Input byte array must not be null.");
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			 GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
			 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream,
				 StandardCharsets.UTF_8))) {

			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			return stringBuilder.toString();
		}
	}
}
