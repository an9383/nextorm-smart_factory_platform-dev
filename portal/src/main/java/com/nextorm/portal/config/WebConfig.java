package com.nextorm.portal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nextorm.portal.config.localdatetime.IsoToLocalDateTimeDeserializer;
import com.nextorm.portal.config.localdatetime.LocalDateTimeToIsoSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	private final ObjectMapper objectMapper;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		ObjectMapper copyMapper = objectMapper.copy();

		SimpleModule module = new SimpleModule();
		module.addDeserializer(LocalDateTime.class, new IsoToLocalDateTimeDeserializer());
		module.addSerializer(LocalDateTime.class, new LocalDateTimeToIsoSerializer());
		copyMapper.registerModule(module);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(copyMapper);

		converters.add(0, converter);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(String.class,
			LocalDateTime.class,
			source -> ZonedDateTime.parse(source)
								   .withZoneSameInstant(ZoneId.systemDefault())
								   .toLocalDateTime());
	}
}
