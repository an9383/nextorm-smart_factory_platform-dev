package com.nextorm.portal.dto.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeSortRequestDto {
	private Long categoryId;
	private List<Long> codeIds = List.of();
}
