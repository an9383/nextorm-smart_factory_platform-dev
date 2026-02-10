package com.nextorm.portal.ai.openai.constant;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public enum Status {
	QUEUED, IN_PROGRESS, REQUIRES_ACTION, CANCELLING, CANCELLED, FAILED, COMPLETED, EXPIRED;

	public static class StatusJsonDeSerializer extends JsonDeserializer<Status> {

		@Override
		public Status deserialize(
			JsonParser jsonParser,
			DeserializationContext deserializationContext
		) throws IOException, JacksonException {
			String key = jsonParser.getText()
								   .toUpperCase();
			return Status.valueOf(key);
		}
	}
}
