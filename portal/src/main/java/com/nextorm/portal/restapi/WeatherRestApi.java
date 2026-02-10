package com.nextorm.portal.restapi;

import com.nextorm.portal.config.properties.WeatherProperties;
import com.nextorm.portal.restapi.dto.weather.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class WeatherRestApi {
	private final WeatherProperties properties;
	private final RestTemplate restTemplate;

	private static final String WEATHER_URL = "http://apis.data.go.kr/1360000/";

	public PresentWeatherDto getPresentWeather(
		String baseDate,
		String baseTime,
		String nx,
		String ny
	) {
		String baseUrl = "VilageFcstInfoService_2.0/getUltraSrtNcst?&dataType=JSON&pageNo=1&numOfRows=1000&base_date=%s&base_time=%s&nx=%s&ny=%s";
		URI url = createURI(String.format(baseUrl, baseDate, baseTime, nx, ny));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, PresentWeatherDto.class)
						   .getBody();
	}

	public ShortForecastWeatherDto getShortForecastWeather(
		String baseDate,
		String baseTime,
		String nx,
		String ny
	) {
		String baseUrl = "VilageFcstInfoService_2.0/getVilageFcst?&dataType=JSON&pageNo=1&numOfRows=1000&base_date=%s&base_time=%s&nx=%s&ny=%s";
		URI url = createURI(String.format(baseUrl, baseDate, baseTime, nx, ny));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, ShortForecastWeatherDto.class)
						   .getBody();
	}

	public VeryShortForecastWeatherDto getVeryShortForecastWeather(
		String baseDate,
		String baseTime,
		String nx,
		String ny
	) {
		String baseUrl = "VilageFcstInfoService_2.0/getUltraSrtFcst?&dataType=JSON&pageNo=1&numOfRows=1000&base_date=%s&base_time=%s&nx=%s&ny=%s";
		URI url = createURI(String.format(baseUrl, baseDate, baseTime, nx, ny));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, VeryShortForecastWeatherDto.class)
						   .getBody();
	}

	public MiddleForcecastWeatherDto getMiddleForcecastWeather(
		String regId,
		String tmFc
	) {
		String baseUrl = "MidFcstInfoService/getMidLandFcst?&dataType=JSON&pageNo=1&numOfRows=1000&regId=%s&tmFc=%s";
		URI url = createURI(String.format(baseUrl, regId, tmFc));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, MiddleForcecastWeatherDto.class)
						   .getBody();
	}

	public MiddleTempWeatherDto getMiddleTempWeather(
		String regId,
		String tmFc
	) {
		String baseUrl = "MidFcstInfoService/getMidTa?&dataType=JSON&pageNo=1&numOfRows=1000&regId=%s&tmFc=%s";
		URI url = createURI(String.format(baseUrl, regId, tmFc));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, MiddleTempWeatherDto.class)
						   .getBody();
	}

	public PastWeatherDto getPastWeather(
		String stnIds,
		String startDt,
		String endDt
	) {
		String baseUrl = "AsosDalyInfoService/getWthrDataList?&dataType=JSON&pageNo=1&numOfRows=999&dataCd=ASOS&dateCd=DAY&stnIds=%s&startDt=%s&endDt=%s";
		URI url = createURI(String.format(baseUrl, stnIds, startDt, endDt));
		HttpEntity<String> entity = createHttpHeadersToEntity();
		return restTemplate.exchange(url, HttpMethod.GET, entity, PastWeatherDto.class)
						   .getBody();
	}

	private HttpEntity<String> createHttpHeadersToEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(headers);
	}

	private URI createURI(String baseUrl) {
		String apiKey = properties.getServiceKey();
		String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
		String fullUrl = WEATHER_URL + baseUrl + "&serviceKey=" + encodedApiKey;
		try {
			return new URI(fullUrl);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid Url: " + fullUrl);
		}
	}
}
