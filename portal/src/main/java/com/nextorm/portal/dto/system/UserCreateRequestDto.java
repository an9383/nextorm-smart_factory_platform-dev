package com.nextorm.portal.dto.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.portal.entity.system.Role;
import com.nextorm.portal.entity.system.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
	private List<Long> roleIds;

	private Long id;
	private String loginId;
	private String name;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	private String email;
	private String phone;
	private String image;

	public User toEntity(
		List<Role> roles,
		byte[] resizedImage,
		String token
	) {
		User user = User.builder()
						.loginId(loginId)
						.password(password)
						.email(email)
						.phone(phone)
						.name(name)
						.image(resizedImage)
						.token(token)
						.build();
		if (roles != null) {
			roles.forEach(role -> user.addUserRole(role));
		}
		return user;
	}

}
