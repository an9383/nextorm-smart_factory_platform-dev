package com.nextorm.portal.dto.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeUpdateRequestDto {
	private String code;
	private String name;
	private String value;
	private String description;
	private Long childCategoryId;
	private List<Long> childCodeIds = List.of();
}
