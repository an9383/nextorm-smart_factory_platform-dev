package com.nextorm.processor.ocap.user;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {
	@Column(name = "login_id",unique = true)
	@Comment("사용자 로그인 ID")
	private String loginId;

	@Comment("이름")
	private String name;

	@Comment("이메일")
	private String email;

	@Comment("연락처")
	private String phone;
}