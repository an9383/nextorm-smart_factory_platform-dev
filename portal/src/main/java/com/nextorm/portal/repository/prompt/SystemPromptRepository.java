package com.nextorm.portal.repository.prompt;

import com.nextorm.portal.entity.prompt.SystemPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemPromptRepository extends JpaRepository<SystemPrompt, Long> {
}
