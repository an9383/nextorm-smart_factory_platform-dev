package com.nextorm.portal.service.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthToken {
	private final String accessToken;
	private final String refreshToken;
}
