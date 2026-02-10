package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ui_languages", indexes = {@Index(name = "idx_ui_languages_message_key", columnList = "message_key"),
	@Index(name = "idx_ui_languages_lang", columnList = "lang")})
@IdClass(UiLanguageId.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class UiLanguage {
	// key는 예약어라서 message_key를 대신 사용한다
	@Id
	@Column(name = "message_key", nullable = false)
	private String key;

	@Id
	@Column(name = "lang", nullable = false)
	private String lang;

	@Column(name = "message", length = 500, nullable = false)
	private String message;

	@Column(name = "create_by", updatable = false)
	private String createBy;

	@CreationTimestamp
	@Column(name = "create_at", nullable = false, updatable = false)
	private LocalDateTime createAt;

	@Column(name = "update_by")
	private String updateBy;

	@UpdateTimestamp
	@Column(name = "update_at")
	private LocalDateTime updateAt;

	@Builder
	public UiLanguage(
		String key,
		String lang,
		String message
	) {
		this.key = key;
		this.lang = lang;
		this.message = message;
	}

	public static UiLanguage create(
		String key,
		String lang,
		String message
	) {
		return UiLanguage.builder()
						 .key(key)
						 .lang(lang)
						 .message(message)
						 .build();
	}

	public void modifyMessage(String message) {
		this.message = message;
	}
}
