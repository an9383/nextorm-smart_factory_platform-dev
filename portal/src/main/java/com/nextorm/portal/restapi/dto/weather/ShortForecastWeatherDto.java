package com.nextorm.portal.restapi.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class ShortForecastWeatherDto {
	private Response response;

	@Data
	public static class Response {
		private Body body;
		private Header header;
	}

	@Data
	public static class Header {
		private String resultCode;
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
		private String fcstValue;
		private String fcstDate;
		private String fcstTime;
	}
}
