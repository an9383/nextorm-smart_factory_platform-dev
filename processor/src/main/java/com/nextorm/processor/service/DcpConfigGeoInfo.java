package com.nextorm.processor.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DcpConfigGeoInfo {
	private boolean isGeoDataType;
	private String latitudeParameterName;
	private String longitudeParameterName;
}
