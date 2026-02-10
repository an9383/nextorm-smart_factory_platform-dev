package com.nextorm.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ChatSession {
	@Id
	@Column(name = "session_id")
	private String sessionId;

	@Column(name = "chat_flow_id", nullable = false)
	private String chatFlowId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "is_favorite", nullable = false)
	private boolean isFavorite;

	@CreationTimestamp
	@Column(name = "create_at", nullable = false, updatable = false)
	private LocalDateTime createAt;

	@UpdateTimestamp
	@Column(name = "update_at")
	private LocalDateTime updateAt;

	public static ChatSession create(
		String sessionId,
		String chatFlowId,
		String title,
		String userId
	) {
		return ChatSession.builder()
						  .sessionId(sessionId)
						  .chatFlowId(chatFlowId)
						  .userId(userId)
						  .title(title)
						  .isFavorite(false)
						  .build();
	}

	public void updateTitle(
		String title
	) {
		this.title = title;
	}

	public void updateFavorite(
		boolean isFavorite
	) {
		this.isFavorite = isFavorite;
	}
}
