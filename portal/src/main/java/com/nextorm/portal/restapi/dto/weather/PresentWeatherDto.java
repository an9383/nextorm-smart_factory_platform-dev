package com.nextorm.portal.restapi.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class PresentWeatherDto {
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
		private String baseDate;
		private String baseTime;
		private String category;
		private int nx;
		private int ny;
		private String obsrValue;
	}
}
