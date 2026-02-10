package com.nextorm.portal.repository.system;

import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.entity.system.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
	Optional<UserSetting> findByUser(User user);
}
