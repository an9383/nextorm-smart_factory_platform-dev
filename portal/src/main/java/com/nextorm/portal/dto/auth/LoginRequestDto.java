package com.nextorm.portal.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDto {
	private String loginId;
	private String password;
}
