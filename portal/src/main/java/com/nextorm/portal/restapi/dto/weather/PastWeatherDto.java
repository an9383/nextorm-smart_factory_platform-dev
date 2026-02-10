package com.nextorm.portal.restapi.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class PastWeatherDto {
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
		private String stnId;
		private String stnNm;
		private String tm;
		private String avgTa;
		private String minTa;
		private String maxTa;
		private String avgWs;
		private String maxWs;
		private String sumRn;
	}
}
