package com.nextorm.portal.config.jpa;

import com.nextorm.portal.service.auth.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

	@Autowired
	SessionService sessionService;

	@Override
	public Optional<String> getCurrentAuditor() {
		String loginId = sessionService.getLoginId();
		if (loginId == null) {
			return Optional.empty();
		}
		return Optional.of(loginId);
	}
}
