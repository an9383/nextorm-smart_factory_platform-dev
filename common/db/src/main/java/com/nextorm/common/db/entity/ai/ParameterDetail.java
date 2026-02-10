package com.nextorm.common.db.entity.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterDetail {
	private Long id;
	private String name;
	private Double min;
	private Double max;
}
