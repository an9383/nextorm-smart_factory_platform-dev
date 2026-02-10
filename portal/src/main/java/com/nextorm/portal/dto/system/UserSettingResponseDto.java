package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingResponseDto {
	private String settingJson;

	public static UserSettingResponseDto from(UserSetting entity) {
		if (entity == null) {
			return null;
		}
		return UserSettingResponseDto.builder()
									 .settingJson(entity.getSettingJson())
									 .build();
	}
}
