package com.nextorm.portal.repository.auth;

import com.nextorm.portal.entity.auth.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
	UserRefreshToken findByToken(String refreshToken);

	void deleteByToken(String refreshToken);

	@Query("""
				DELETE FROM UserRefreshToken urt
				WHERE urt.expiration < :localDateTime 
		""")
	@Modifying
	void deleteByExpirationBefore(@Param("localDateTime") LocalDateTime localDateTime);

}
