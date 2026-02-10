package com.nextorm.portal.repository.system;

import com.nextorm.portal.entity.system.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
