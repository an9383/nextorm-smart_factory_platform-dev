package com.nextorm.portal.service.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
	/**
	 * 현재 로그인 된 유저ID를 가져옴
	 *
	 * @return
	 */
	public String getLoginId() {
		Authentication authentication = SecurityContextHolder.getContext()
															 .getAuthentication();
		if (authentication != null) {
			return authentication.getName();
		}
		return null;
	}

	/**
	 * 현재 로그인 된 유저의 ROLE을 가져옴
	 *
	 * @return
	 */
	public String getRole() {
		Authentication authentication = SecurityContextHolder.getContext()
															 .getAuthentication();
		if (authentication != null) {
			return authentication.getAuthorities()
								 .iterator()
								 .next()
								 .getAuthority();
		}
		return null;
	}
}
