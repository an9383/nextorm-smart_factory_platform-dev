package com.nextorm.portal.dto.parameterdata;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecipeTrendRequestDto {
	private Long toolId;
	private Long recipeParameterId;
	private String recipeName;
	private Long parameterId;
	private LocalDateTime from;
	private LocalDateTime to;
}
