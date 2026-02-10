package com.nextorm.portal.restapi;

import com.nextorm.portal.restapi.dto.openstreetmap.ReverseGeocodingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class OpenStreetMapRestApi {
	private final RestTemplate restTemplate;

	private static final String BASE_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f";

	public ReverseGeocodingDto reverseGeocoding(
		double lat,
		double lon
	) {
		String url = String.format(BASE_URL, lat, lon);
		HttpHeaders headers = createHttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, ReverseGeocodingDto.class)
						   .getBody();
	}

	private HttpHeaders createHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
