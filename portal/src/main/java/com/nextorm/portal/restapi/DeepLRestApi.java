package com.nextorm.portal.restapi;

import com.nextorm.portal.config.properties.DeepLProperties;
import com.nextorm.portal.restapi.dto.deepl.TranslationRequestDto;
import com.nextorm.portal.restapi.dto.deepl.TranslationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeepLRestApi {

	private final DeepLProperties properties;
	private final RestTemplate restTemplate;
	private final String TRANSLATE_API_URL = "https://api-free.deepl.com/v2/translate?target_lang=%s";

	public TranslationResponseDto translate(
		String targetLang,
		List<String> text
	) {

		TranslationRequestDto translationRequest = TranslationRequestDto.builder()
																		.targetLang(targetLang)
																		.text(text)
																		.build();
		String url = String.format(TRANSLATE_API_URL, translationRequest.getTargetLang());
		HttpHeaders headers = createHttpHeaders();

		TranslationResponseDto response = restTemplate.postForObject(url,
			createHttpEntity(translationRequest, headers),
			TranslationResponseDto.class);

		return response;
	}

	private HttpHeaders createHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "DeepL-Auth-Key " + properties.getServiceKey());
		return headers;
	}

	private HttpEntity<TranslationRequestDto> createHttpEntity(
		TranslationRequestDto translationRequestDto,
		HttpHeaders headers
	) {

		return new HttpEntity<>(translationRequestDto, headers);
	}
}
