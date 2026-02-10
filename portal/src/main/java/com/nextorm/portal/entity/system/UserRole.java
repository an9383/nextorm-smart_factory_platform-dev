package com.nextorm.portal.entity.system;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user_role")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	@Comment("권한")
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@Comment("사용자")
	private User user;

	public static UserRole create(
		User user,
		Role role
	) {
		return UserRole.builder()
					   .user(user)
					   .role(role)
					   .build();
	}
}
