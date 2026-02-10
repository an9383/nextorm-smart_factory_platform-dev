package com.nextorm.portal.restapi;

import com.nextorm.portal.restapi.dto.openstreetmap.OverpassSearchDto;
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
public class OverpassRestApi {
	private final RestTemplate restTemplate;

	private static final String OVERPASS_URL = "http://overpass-api.de/api/interpreter?data=%s";

	public OverpassSearchDto getOverpassInfraData(
		double lat,
		double lon,
		long radius
	) {
		String query = createQuery(lat, lon, radius);
		String url = String.format(OVERPASS_URL, query);
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, OverpassSearchDto.class)
						   .getBody();
	}

	private String createQuery(
		double lat,
		double lon,
		long radius
	) {
		StringBuilder queryBuilder = new StringBuilder("[out:json];");
		queryBuilder.append("(");
		queryBuilder.append(String.format("node(around:%d,%f,%f)[amenity=restaurant];", radius, lat, lon));
		queryBuilder.append(String.format("node(around:%d,%f,%f)[amenity=cafe];", radius, lat, lon));
		queryBuilder.append(String.format("node(around:%d,%f,%f)[amenity=food_court];", radius, lat, lon));
		queryBuilder.append(String.format("node(around:%d,%f,%f)[landuse=industrial];", radius, lat, lon));
		queryBuilder.append(String.format("way(around:%d,%f,%f)[landuse=industrial];", radius, lat, lon));
		queryBuilder.append(");");
		queryBuilder.append("out body geom;");
		return queryBuilder.toString();
	}

	private HttpEntity<String> createHttpHeadersToEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(headers);
	}
}
