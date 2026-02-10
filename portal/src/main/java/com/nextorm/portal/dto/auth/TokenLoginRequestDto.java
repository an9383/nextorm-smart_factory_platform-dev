package com.nextorm.portal.dto.auth;

import lombok.Data;

@Data
public class TokenLoginRequestDto {
	private String loginId;
	private String token;
}
