package com.nextorm.extensions.misillan.alarm.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.extensions.misillan.alarm.config.properties.EqmsPortalSourceConfig;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class EqmsPortalClient {
	// 토큰 만료 보정 여유시간(초). 이 시간 이내로 만료되는 토큰은 재발급하도록 처리합니다.
	private static final long EXPIRATION_MARGIN_SECONDS = 30L;

	private final EqmsPortalSourceConfig sourceConfig;
	private final RestTemplate restTemplate;
	private final JwtParser jwtParser;

	private final Object lock = new Object();    // 동기화용 락 오브젝트
	private TokenInfo tokenInfo = null;

	/**
	 * 포탈 모듈에서 Tool 목록을 가져오는 메소드
	 *
	 * @return ToolDto 목록
	 */
	public List<ToolDto> getTools() {

		log.info("포탈 모듈에서 파라미터 목록을 조회합니다.");

		String accessToken = getAccessToken();
		if (accessToken == null) {
			log.warn("유효한 access_token이 없어 파라미터 목록을 조회할 수 없습니다.");
			return List.of();
		}

		log.info("포탈 모듈에서 Tool 목록을 조회합니다.");

		// HTTP 헤더 설정 (인증 토큰 포함)
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		// HTTP 요청 엔티티 생성
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		try {
			// Tool 목록 API 호출
			String toolsUrl = sourceConfig.getUrl() + "/api/tool/tools/with-location-user";
			ResponseEntity<ToolDto[]> response = restTemplate.exchange(toolsUrl,
				HttpMethod.GET,
				requestEntity,
				ToolDto[].class);

			if (response.getBody() == null) {
				log.warn("Tool 목록 응답이 비어있습니다.");
				return List.of();
			}

			List<ToolDto> tools = Arrays.asList(response.getBody());
			log.info("Tool 목록 조회 완료. 총 {}개 Tool을 가져왔습니다.", tools.size());
			return tools;

		} catch (Exception e) {
			log.error("Tool 목록 조회 중 오류가 발생했습니다.", e);
			return List.of();
		}
	}

	/**
	 * 포탈 모듈에서 Product 목록을 가져오는 메소드
	 *
	 * @return ProductDto 목록
	 */
	public List<ProductDto> getProducts() {

		log.info("포탈 모듈에서 Product 목록을 조회합니다.");

		String accessToken = getAccessToken();
		if (accessToken == null) {
			log.warn("유효한 access_token이 없어 파라미터 목록을 조회할 수 없습니다.");
			return List.of();
		}

		// HTTP 헤더 설정 (인증 토큰 포함)
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		// HTTP 요청 엔티티 생성
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		try {
			// Product 목록 API 호출
			String productsUrl = sourceConfig.getUrl() + "/api/product/products";
			ResponseEntity<ProductDto[]> response = restTemplate.exchange(productsUrl,
				HttpMethod.GET,
				requestEntity,
				ProductDto[].class);

			if (response.getBody() == null) {
				log.warn("Product 목록 응답이 비어있습니다.");
				return List.of();
			}

			List<ProductDto> products = Arrays.asList(response.getBody());
			log.info("Product 목록 조회 완료. 총 {}개 Product를 가져왔습니다.", products.size());
			return products;

		} catch (Exception e) {
			log.error("Product 목록 조회 중 오류가 발생했습니다.", e);
			return List.of();
		}
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

		log.info("EQMS 토큰 만료시간: {}", tokenInfo.expiredAt());
		LocalDateTime expiration = tokenInfo.expiredAt();
		LocalDateTime marginNow = LocalDateTime.now()
											   .plusSeconds(EXPIRATION_MARGIN_SECONDS);
		// 현재시간 + 마진시간이 만료시간 이전이면 유효한 토큰
		return marginNow.isBefore(expiration);
	}

	/**
	 * 포탈 모듈에 로그인하여 access_token을 받아옵니다.
	 *
	 * @return access_token 문자열, 로그인 실패 시 null
	 */
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
				log.info("포탈 모듈 로그인 성공. access_token을 받았습니다.");
				LocalDateTime expiration = jwtParser.parseExpiration(accessToken);
				tokenInfo = new TokenInfo(accessToken, expiration);
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
	 * Tool 정보를 담는 DTO 클래스
	 */
	@Data
	public static class ToolDto {
		private Long id;
		private Long userId;
		private String userName;
		private String type;
		private String standard;
		private String name;
		private String status;
		private String ext;
		@JsonProperty("createAt")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime createdAt;
		@JsonProperty("updateAt")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime updatedAt;
	}

	/**
	 * Product 정보를 담는 DTO 클래스
	 */
	@Data
	public static class ProductDto {
		private Long id;
		private String name;
		private String type;
		private String subType;
		private String unit;
		private Double unitPrice;
		private Long bizPartnerId;
		private String bizPartnerName;
		private String bizPartnerCode;
		private String description;
		private String barcode;
		private String ext;
		@JsonProperty("createAt")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime createdAt;
		@JsonProperty("updateAt")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime updatedAt;
	}
}
