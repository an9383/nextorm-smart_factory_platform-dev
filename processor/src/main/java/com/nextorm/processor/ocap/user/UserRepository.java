package com.nextorm.processor.ocap.user;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findByIdIn(@Param("userIds") List<Long> userIds);
}
