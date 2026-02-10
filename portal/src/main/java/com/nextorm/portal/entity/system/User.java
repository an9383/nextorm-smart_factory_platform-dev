package com.nextorm.portal.entity.system;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "user")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,
		CascadeType.PERSIST}, orphanRemoval = true)
	@Comment("사용자와 권한 연결되는 테이블")
	@Builder.Default
	private Set<UserRole> userRoles = new HashSet<>();

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,
		CascadeType.PERSIST}, orphanRemoval = true)
	@Comment("사용자 설정 테이블")
	@Builder.Default
	private UserSetting userSetting = new UserSetting();

	@Column(unique = true)
	@Comment("사용자 로그인 ID")
	private String loginId;

	@Comment("비밀번호 해시 값")
	private String password;

	@Comment("토큰 해시값")
	private String token;

	@Comment("이름")
	private String name;

	@Comment("이메일")
	private String email;

	@Comment("연락처")
	private String phone;

	@Lob
	@Comment("프로필 이미지")
	@Column(columnDefinition = "LONGBLOB")
	private byte[] image;

	@Comment("언어")
	@Column(name = "locale")
	private Locale locale;

	public String getImage() {
		if (image == null) {
			return "";
		}
		return "data:image/jpeg;base64," + java.util.Base64.getEncoder()
														   .withoutPadding()
														   .encodeToString(image);
	}

	public void clearUserRole() {
		this.userRoles.clear();
	}

	public void addUserRole(Role role) {
		this.userRoles.add(UserRole.create(this, role));
	}

	public void modify(
		String password,
		String name,
		String email,
		String phone,
		byte[] image,
		List<Role> roles

	) {
		if (password != null) {
			this.password = password;
		}
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.image = image;
		this.clearUserRole();
		roles.forEach(this::addUserRole);
	}

	public void modifyPassword(
		String password
	) {
		this.password = password;
	}

	public void changeLocale(Locale locale) {
		this.locale = locale;
	}

	public void updateToken(String token){
		this.token = token;
	}
}