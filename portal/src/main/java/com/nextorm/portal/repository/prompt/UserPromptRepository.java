package com.nextorm.portal.repository.prompt;

import com.nextorm.portal.entity.prompt.UserPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPromptRepository extends JpaRepository<UserPrompt, Long> {
	List<UserPrompt> findByUserIdOrderBySort(@Param("userId") String userId);
}
