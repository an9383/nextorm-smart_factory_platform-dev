package com.nextorm.portal.dto.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 메타데이터 타입 파라미터의 일괄 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDataParameterBulkUpdateRequestDto {
	/**
	 * 파라미터 ID
	 */
	private Long id;
	
	/**
	 * 수정할 값
	 */
	private String value;
}
