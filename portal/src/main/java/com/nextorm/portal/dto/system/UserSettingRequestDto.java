package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.UserSetting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingRequestDto {
	private String settingJson;

	public UserSetting toEntity(String userLoginId) {
		return UserSetting.builder()
						  .settingJson(settingJson)
						  .createBy(userLoginId)
						  .updateBy(userLoginId)
						  .build();
	}
}
