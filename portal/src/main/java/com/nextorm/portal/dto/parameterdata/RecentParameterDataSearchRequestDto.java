package com.nextorm.portal.dto.parameterdata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecentParameterDataSearchRequestDto {
	private Long lastDataId;
	private Long parameterId;
	private Integer limit;
}
