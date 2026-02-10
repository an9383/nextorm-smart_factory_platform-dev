package com.nextorm.apcmodeling.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component
@ConfigurationProperties(prefix = "spring.auth.jwt")
public class JWTProperties {
	private String secretKey;
	private int accessTokenExpirationSecs;
	private int refreshTokenExpirationSecs;
	private String accessTokenHeader;
	private String accessTokenHeaderPrefix;
	private String refreshTokenCookieName;
	private String collectorApiToken;
	private List<String> collectorApiUri = List.of();
	private String aidasApiToken;
	private List<String> aidasApiUri = List.of();

	/**
	 * Collector API 요청인지 확인
	 *
	 * @param accessToken accessToken
	 * @param requestUri  요청 uri
	 * @return
	 */
	public boolean isPermitAsCollector(
		String accessToken,
		String requestUri
	) {
		if (accessToken == null || !accessToken.equals(collectorApiToken)) {
			return false;
		}

		return collectorApiUri.stream()
							  .anyMatch(uri -> {
								  Pattern pattern = Pattern.compile(this.replaceAsteriskToRegexp(uri));
								  Matcher matcher = pattern.matcher(requestUri);
								  return matcher.matches();
							  });
	}

	/**
	 * AiDas API 요청인지 확인
	 *
	 * @param accessToken accessToken
	 * @param requestUri  요청 uri
	 * @return
	 */
	public boolean isPermitAsAiDas(
		String accessToken,
		String requestUri
	) {
		if (accessToken == null || !accessToken.equals(aidasApiToken)) {
			return false;
		}
		return aidasApiUri.stream()
						  .anyMatch(uri -> {
							  Pattern pattern = Pattern.compile(this.replaceAsteriskToRegexp(uri));
							  Matcher matcher = pattern.matcher(requestUri);
							  return matcher.matches();
						  });
	}

	/**
	 * 입력된 uri 의 **, *을 정규표현식에 맞도록 변환
	 *
	 * @param uri
	 * @return
	 */
	private String replaceAsteriskToRegexp(String uri) {
		return uri.replace("**", ".+")
				  .replace("*", "[^/]+");
	}
}