package com.nextorm.portal.entity.system;

import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.portal.dto.system.UserSettingRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "user_setting")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserSetting extends BaseEntity {

	@Column(length = 10240, name = "setting_json")
	@Comment("설정 정보(JSON)")
	private String settingJson;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@Comment("사용자")
	private User user;

	public void createOrModify(
		UserSettingRequestDto dto,
		User user
	) {
		this.settingJson = dto.getSettingJson();
		this.user = user;
	}

}
