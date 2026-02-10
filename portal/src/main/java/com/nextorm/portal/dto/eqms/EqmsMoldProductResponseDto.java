package com.nextorm.portal.dto.eqms;

import lombok.Builder;
import lombok.Data;

/**
 * 종속 사이트: 건창스치로폴
 * <p>
 * 금형으로 생산하는 제품 정보
 */
@Data
@Builder
public class EqmsMoldProductResponseDto {
	private Long id;
	private String name;
	private String code;
	private Integer cavity;
	private Integer cycleCount;             // 타수
	private Integer averageProductionCount;    // 일 평균 생산 수량
}
