package com.nextorm.portal.entity.prompt;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user_prompt", indexes = @Index(name = "idx_user_prompt_user_id", columnList = "user_id"))
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPrompt extends BaseEntity {

	@Comment("사용자 메세지")
	@Column(name = "message", nullable = false)
	private String message;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Setter
	@Comment("정렬 순서")
	private Integer sort;

	public static UserPrompt create(
		String message,
		String userId,
		Integer sort
	) {

		return UserPrompt.builder()
						 .message(message)
						 .userId(userId)
						 .sort(sort)
						 .build();
	}

	public void updateMessage(String message) {
		this.message = message;
	}
}
