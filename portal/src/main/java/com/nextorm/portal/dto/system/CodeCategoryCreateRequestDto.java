package com.nextorm.portal.dto.system;

import com.nextorm.common.db.entity.system.code.CodeCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeCategoryCreateRequestDto {
	private String category;
	private String name;
	private String description;

	public CodeCategory toEntity() {
		return CodeCategory.builder()
						   .category(category)
						   .name(name)
						   .description(description)
						   .build();
	}
}
