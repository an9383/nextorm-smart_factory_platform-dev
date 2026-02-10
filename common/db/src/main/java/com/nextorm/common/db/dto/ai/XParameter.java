package com.nextorm.common.db.dto.ai;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class XParameter {
	private Long id;
	private Double min;
	private Double max;
}
