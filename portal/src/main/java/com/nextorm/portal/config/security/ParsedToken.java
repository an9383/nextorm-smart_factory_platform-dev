package com.nextorm.portal.config.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class ParsedToken {
	private String loginId;
	private String role;
	private boolean isAccessToken;
	private Date expiration;
	private Date issuedAt;
}
