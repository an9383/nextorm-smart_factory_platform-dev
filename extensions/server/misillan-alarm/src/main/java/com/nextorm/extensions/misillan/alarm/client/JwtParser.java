package com.nextorm.extensions.misillan.alarm.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtParser {
	private final ObjectMapper objectMapper;

	public LocalDateTime parseExpiration(String jwt) {
		if (jwt == null || jwt.isBlank()) {
			return null;
		}

		try {
			String[] parts = jwt.split("\\.");
			if (parts.length < 2) {
				log.warn("invalid jwt format: missing payload part");
				return null;
			}

			String payloadB64 = parts[1];
			byte[] decoded = Base64.getUrlDecoder()
								   .decode(payloadB64);
			JsonNode payload = objectMapper.readTree(decoded);

			JsonNode expNode = payload.get("exp");
			if (expNode == null || expNode.isNull()) {
				return null;
			}

			Instant instant = Instant.ofEpochSecond(expNode.longValue());
			return LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		} catch (Exception e) {
			log.warn("failed to parse jwt expiration", e);
			return null;
		}
	}
}
