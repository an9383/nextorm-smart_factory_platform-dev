package com.nextorm.portal.scheduler;

import com.nextorm.portal.repository.auth.UserRefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpiredRefreshTokenDeleteScheduler {

	private final UserRefreshTokenRepository userRefreshTokenRepository;

	/**
	 * 현시간 기준 만료된 refresh token을 cron 주기로 삭제한다
	 */
	@Transactional
	@Scheduled(cron = "${scheduler.cron.delete-expired-refresh-token}")
	public void deleteExpiredRefreshToken() {
		LocalDateTime nowDateTime = LocalDateTime.now();
		userRefreshTokenRepository.deleteByExpirationBefore(nowDateTime);
		log.info("deleted expired refresh token.");
	}
}
