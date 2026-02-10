package com.nextorm.portal.restapi.dto.kakaomap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoMapRequestDto {
	private String query;
	private Double latitude;
	private Double longitude;
	private Long radius;
	private Long page;
	private String sort;
	private Long size;
}
