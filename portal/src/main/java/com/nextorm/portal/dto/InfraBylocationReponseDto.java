package com.nextorm.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InfraBylocationReponseDto {
	public static enum Type {
		RESTAURANT, CAFE, INDUSTRIAL;
	}

	private String name;
	private Type type;
	private double lat;
	private double lon;
}
