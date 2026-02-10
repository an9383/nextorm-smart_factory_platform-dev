package com.nextorm.portal.dto.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeCategoryUpdateRequestDto {
	private String name;
	private String description;
}
