package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserResponseDto {
	private Long id;
	private String loginId;
	private String name;
	private String email;
	private String phone;
	private String image;
	private List<RoleResponseDto> roles;
	private UserSettingResponseDto userSetting;
	private Locale locale;

	public static LoginUserResponseDto from(User entity) {
		if (entity == null) {
			return null;
		}

		return LoginUserResponseDto.builder()
								   .id(entity.getId())
								   .loginId(entity.getLoginId())
								   .email(entity.getEmail())
								   .name(entity.getName())
								   .phone(entity.getPhone())
								   .image(entity.getImage())
								   .roles(entity.getUserRoles()
												.stream()
												.map(userRole -> RoleResponseDto.from(userRole.getRole()))
												.toList())
								   .userSetting(UserSettingResponseDto.from(entity.getUserSetting()))
								   .locale(entity.getLocale())
								   .build();
	}
}