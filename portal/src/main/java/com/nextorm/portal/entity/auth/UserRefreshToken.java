package com.nextorm.portal.entity.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_refresh_token")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login_id")
	@Comment("사용자 로그인 ID")
	private String loginId;

	@Column(unique = true)
	@Comment("Refresh 토큰")
	private String token;

	@Column(name = "expired_at")
	@Comment("토큰 만료 시간")
	private LocalDateTime expiration;

	@Column(name = "issued_at")
	@Comment("토큰 생성 시간")
	private LocalDateTime issuedAt;
}
