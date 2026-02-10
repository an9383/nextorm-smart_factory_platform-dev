package com.nextorm.summarizer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeoDataDto {
	private double latitude;
	private double longitude;
}
