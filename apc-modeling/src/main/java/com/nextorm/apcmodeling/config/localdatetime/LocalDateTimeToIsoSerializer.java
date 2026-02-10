package com.nextorm.apcmodeling.config.localdatetime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeToIsoSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(
		LocalDateTime localDateTime,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
		String isoString = DateTimeFormatter.ISO_INSTANT.format(zonedDateTime);
		jsonGenerator.writeString(isoString);
	}
}