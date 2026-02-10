package com.nextorm.extensions.misillan.alarm.client;

import com.nextorm.extensions.misillan.alarm.config.properties.SfpPortalSourceConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SfpPortalClient {
	// 토큰 만료 보정 여유시간(초). 이 시간 이내로 만료되는 토큰은 재발급하도록 처리합니다.
	private static final long EXPIRATION_MARGIN_SECONDS = 30L;

	private final SfpPortalSourceConfig sourceConfig;
	private final RestTemplate restTemplate;
	private final JwtParser jwtParser;

	private final Object lock = new Object();    // 동기화용 락 오브젝트
	private TokenInfo tokenInfo = null;

	/**
	 * 포탈 모듈에서 파라미터 목록을 가져와서 필요한 필드만 반환하는 메소드
	 *
	 * @return SfpParameter 목록 (dataType이 유효한 것만)
	 */
	public List<SfpParameterDto> getParameters() {

		log.info("포탈 모듈에서 파라미터 목록을 조회합니다.");

		String accessToken = getAccessToken();
		if (accessToken == null) {
			log.warn("유효한 access_token이 없어 파라미터 목록을 조회할 수 없습니다.");
			return List.of();
		}

		HttpEntity<Void> requestEntity = createRequestEntity(accessToken);

		try {
			// 파라미터 목록 API 호출
			String parametersUrl = sourceConfig.getUrl() + "/api/parameters";
			ResponseEntity<ParameterResponseDto[]> response = restTemplate.exchange(parametersUrl,
				HttpMethod.GET,
				requestEntity,
				ParameterResponseDto[].class);

			if (response.getBody() == null) {
				log.warn("파라미터 목록 응답이 비어있습니다.");
				return List.of();
			}

			// 응답 데이터를 SfpParameterDto로 변환 (dataType 필터링 포함)
			List<SfpParameterDto> result = Arrays.stream(response.getBody())
												 .filter(param -> isValidDataType(param.getDataType()))
												 .map(this::convertToSfpParameterDto)
												 .toList();

			log.info("파라미터 목록 조회 완료. 총 {}개 파라미터를 가져왔습니다.", result.size());
			return result;

		} catch (Exception e) {
			log.error("파라미터 목록 조회 중 오류가 발생했습니다.", e);
			return List.of();
		}
	}

	public Map<String, Map<String, String>> getI18nData() {
		String accessToken = getAccessToken();
		if (accessToken == null) {
			log.warn("유효한 access_token이 없어 i18n 정보를 조회할 수 없습니다.");
			return Map.of();
		}

		HttpEntity<Void> requestEntity = createRequestEntity(accessToken);

		try {
			// i18n 정보 API 호출
			String i18nUrl = sourceConfig.getUrl() + "/api/i18n";
			ResponseEntity<Map> response = restTemplate.exchange(i18nUrl, HttpMethod.GET, requestEntity, Map.class);
			if (response.getBody() == null) {
				log.warn("i18n 정보 응답이 비어있습니다.");
				return Map.of();
			}
			return response.getBody();

		} catch (Exception e) {
			log.error("i18n 정보 조회 중 오류가 발생했습니다.", e);
			return Map.of();
		}
	}

	/**
	 * 인증 헤더가 포함된 HttpEntity 생성
	 */
	private HttpEntity<Void> createRequestEntity(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		return new HttpEntity<>(headers);
	}

	/**
	 * DataType이 유효한 값인지 확인 (INTEGER, DOUBLE만 허용)
	 */
	private boolean isValidDataType(Object dataType) {
		if (dataType == null) {
			return false;
		}
		String dataTypeStr = dataType.toString();
		return "INTEGER".equals(dataTypeStr) || "DOUBLE".equals(dataTypeStr);
	}

	/**
	 * ParameterResponseDto를 SfpParameterDto로 변환
	 */
	private SfpParameterDto convertToSfpParameterDto(ParameterResponseDto parameterDto) {
		SfpParameterDto sfpParam = new SfpParameterDto();
		sfpParam.setId(parameterDto.getId());
		sfpParam.setName(parameterDto.getName());
		sfpParam.setUpdatedAt(parameterDto.getUpdateAt());

		// dataType을 문자열로 변환하여 저장
		if (parameterDto.getDataType() != null) {
			String dataTypeStr = parameterDto.getDataType()
											 .toString();
			if ("INTEGER".equals(dataTypeStr) || "DOUBLE".equals(dataTypeStr)) {
				sfpParam.setDataType(dataTypeStr);
			}
		}

		return sfpParam;
	}

	/**
	 * 포탈 모듈에 로그인하여 access_token을 얻는 메소드
	 * <p>
	 * 토큰이 유효하면 기존 토큰을 반환하고, 만료되었으면 재발급 요청을 합니다.
	 * 동시성 문제를 막기 위해 synchronized 블록을 사용합니다.
	 *
	 * @return access_token 문자열, 실패시 null
	 */
	private synchronized String getAccessToken() {
		if (validateToken()) {
			return tokenInfo.token();
		}

		synchronized (lock) {
			// 더블 체크
			if (validateToken()) {
				return tokenInfo.token();
			}
			return requestLoginProcess();
		}
	}

	private boolean validateToken() {
		if (tokenInfo == null) {
			return false;
		}

		LocalDateTime expiration = tokenInfo.expiredAt();
		LocalDateTime marginNow = LocalDateTime.now()
											   .plusSeconds(EXPIRATION_MARGIN_SECONDS);
		// 현재시간 + 마진시간이 만료시간 이전이면 유효한 토큰
		return marginNow.isBefore(expiration);
	}

	private String requestLoginProcess() {
		log.info("포탈 모듈 로그인을 시도합니다. URL: {}", sourceConfig.getUrl());
		// 로그인 요청 데이터 생성
		LoginRequestDto loginRequest = new LoginRequestDto();
		loginRequest.setLoginId(sourceConfig.getId());
		loginRequest.setPassword(sourceConfig.getPassword());

		// HTTP 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		// HTTP 요청 엔티티 생성
		HttpEntity<LoginRequestDto> requestEntity = new HttpEntity<>(loginRequest, headers);

		try {
			// 포탈 모듈의 로그인 API 호출
			String loginUrl = sourceConfig.getUrl() + "/api/auth/login";
			ResponseEntity<Void> response = restTemplate.exchange(loginUrl, HttpMethod.POST, requestEntity, Void.class);

			// 응답 헤더에서 access_token 추출
			HttpHeaders responseHeaders = response.getHeaders();
			String authorizationHeader = responseHeaders.getFirst("Authorization");

			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				String accessToken = authorizationHeader.substring(7); // "Bearer " 제거
				// 만료시간 파싱 (JwtParser 사용)
				LocalDateTime expiration = jwtParser.parseExpiration(accessToken);
				tokenInfo = new TokenInfo(accessToken, expiration);
				log.info("포탈 모듈 로그인 성공. access_token을 받았습니다. 만료: {}", expiration);
				return accessToken;
			}

			log.warn("응답 헤더에서 Authorization 헤더를 찾을 수 없습니다.");
			return null;

		} catch (Exception e) {
			log.error("포탈 모듈 로그인 중 오류가 발생했습니다.", e);
			return null;
		}
	}

	/**
	 * 로그인 요청 DTO 클래스
	 */
	@Data
	public static class LoginRequestDto {
		private String loginId;
		private String password;
	}

	/**
	 * 포탈 모듈의 파라미터 응답 DTO (필요한 필드만)
	 */
	@Data
	public static class ParameterResponseDto {
		private Long id;
		private String name;
		private LocalDateTime updateAt;
		private Object dataType; // enum을 Object로 받아서 나중에 변환
	}

	@Data
	public static class SfpParameterDto {
		private Long id;
		private String name;
		private LocalDateTime updatedAt;
		private String dataType; // enum 대신 String 사용
	}
}
