package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	private Long id;
	private String loginId;
	private String name;
	private String email;
	private String phone;
	private String image;
	private String token;
	private List<Long> roleIds;

	public static UserResponseDto from(User entity) {
		if (entity == null) {
			return null;
		}
		UserResponseDto.UserResponseDtoBuilder builder = UserResponseDto.builder()
																		.id(entity.getId())
																		.loginId(entity.getLoginId())
																		.email(entity.getEmail())
																		.name(entity.getName())
																		.phone(entity.getPhone())
																		.image(entity.getImage())
																		.token(entity.getToken())
																		.roleIds(entity.getUserRoles()
																					   .stream()
																					   .map(userRole -> userRole.getRole()
																												.getId())
																					   .collect(Collectors.toList()));
		return builder.build();
	}
}
