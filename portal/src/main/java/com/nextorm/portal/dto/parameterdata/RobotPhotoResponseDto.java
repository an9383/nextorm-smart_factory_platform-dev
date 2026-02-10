package com.nextorm.portal.dto.parameterdata;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RobotPhotoResponseDto {
	private GPSCoordinateDto coordinates;
	private String imageUrl;
	private String caption;

	public static RobotPhotoResponseDto of(
		GPSCoordinateDto key,
		String imageUrl,
		LocalDateTime traceAt
	) {
		return RobotPhotoResponseDto.builder()
									.coordinates(key)
									.imageUrl(imageUrl)
									.caption(traceAt.toString())
									.build();
	}
}
