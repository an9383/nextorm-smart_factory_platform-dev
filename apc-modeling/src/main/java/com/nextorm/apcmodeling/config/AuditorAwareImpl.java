package com.nextorm.apcmodeling.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext()
															 .getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return Optional.empty();
		}
		return Optional.of(authentication.getName());
	}
}
