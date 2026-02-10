package com.nextorm.portal.restapi.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class MiddleTempWeatherDto {
	private Response response;

	@Data
	public static class Response {
		private Body body;
	}

	@Data
	public static class Body {
		private Items items;
	}

	@Data
	public static class Items {
		private List<Item> item;
	}

	@Data
	public static class Item {
		private String regId;
		private String taMin3;
		private String taMax3;
		private String taMin4;
		private String taMax4;
		private String taMin5;
		private String taMax5;
		private String taMin6;
		private String taMax6;
		private String taMin7;
		private String taMax7;
		private String taMin8;
		private String taMax8;
		private String taMin9;
		private String taMax9;
		private String taMin10;
		private String taMax10;
	}
}
