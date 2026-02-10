package com.nextorm.portal.restapi.dto.openstreetmap;

import lombok.Data;

@Data
public class ReverseGeocodingDto {
	private Double lat;
	private Double lon;
	private String displayName;
	private Address address;

	@Data
	public static class Address {
		private String city;
		private String county;

	}
}
