package com.nextorm.apcmodeling.config.localdatetime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IsoToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

	@Override
	public LocalDateTime deserialize(
		JsonParser p,
		DeserializationContext ctxt
	) throws IOException {
		String isoString = p.getText();
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME);
		return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
							.toLocalDateTime();
	}
}