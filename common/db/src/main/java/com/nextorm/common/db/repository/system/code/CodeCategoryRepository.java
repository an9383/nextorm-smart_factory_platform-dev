package com.nextorm.common.db.repository.system.code;

import com.nextorm.common.db.entity.system.code.CodeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeCategoryRepository extends JpaRepository<CodeCategory, Long> {
	Optional<CodeCategory> findByCategory(String category);
}
