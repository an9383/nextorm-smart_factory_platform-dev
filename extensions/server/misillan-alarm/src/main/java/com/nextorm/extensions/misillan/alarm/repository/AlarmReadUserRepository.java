package com.nextorm.extensions.misillan.alarm.repository;

import com.nextorm.extensions.misillan.alarm.entity.AlarmReadUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmReadUserRepository extends JpaRepository<AlarmReadUser, Long> {
}
