package com.nextorm.portal.dto.aimodel.correlation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationRankDto {
	private Long parameterId;
	private String parameterName;
	private int rank;
	private String corCoefficient;
	private List<Integer> paramX;
	private List<Integer> paramY;
}
