package com.nextorm.extensions.proxystatusgateway.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 서버 정보를 담는 DTO 클래스
 * JSON 설정 파일에서 읽어올 서버의 이름과 IP 주소 정보
 */
@Data
@EqualsAndHashCode
public class ServerInfo {

	@NotBlank(message = "서버 이름은 필수입니다")
	@JsonProperty("name")
	private String name;

	@NotBlank(message = "IP 주소는 필수입니다")
	@Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "올바른 IP 주소 형식이 아닙니다")
	@JsonProperty("ip")
	private String ip;
}
