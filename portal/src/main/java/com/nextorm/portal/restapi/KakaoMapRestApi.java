package com.nextorm.portal.restapi;

import com.nextorm.portal.config.properties.KakaoProperties;
import com.nextorm.portal.restapi.dto.kakaomap.KakaoMapRequestDto;
import com.nextorm.portal.restapi.dto.kakaomap.KakaoQuerySearchDto;
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
public class KakaoMapRestApi {

	private final KakaoProperties properties;
	private final RestTemplate restTemplate;

	private static final String KAKAO_URL = "https://dapi.kakao.com/v2/local/search/keyword.json?query=%s&y=%f&x=%f&radius=%d&page=%d&sort=%s&size=%d";

	public KakaoQuerySearchDto getKakaoMapInfraData(KakaoMapRequestDto requestDto) {
		String url = String.format(KAKAO_URL,
			requestDto.getQuery(),
			requestDto.getLatitude(),
			requestDto.getLongitude(),
			requestDto.getRadius(),
			requestDto.getPage(),
			requestDto.getSort(),
			requestDto.getSize());
		HttpHeaders headers = createHttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(url, HttpMethod.GET, entity, KakaoQuerySearchDto.class)
						   .getBody();
	}

	private HttpHeaders createHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "KakaoAK " + properties.getServiceKey());
		return headers;
	}

}
