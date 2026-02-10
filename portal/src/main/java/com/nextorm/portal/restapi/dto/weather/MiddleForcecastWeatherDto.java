package com.nextorm.portal.restapi.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class MiddleForcecastWeatherDto {
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

		private String rnSt3Am;

		private String rnSt3Pm;

		private String rnSt4Am;

		private String rnSt4Pm;

		private String rnSt5Am;

		private String rnSt5Pm;

		private String rnSt6Am;

		private String rnSt6Pm;

		private String rnSt7Am;

		private String rnSt7Pm;

		private String rnSt8;

		private String rnSt9;

		private String rnSt10;

		private String wf3Am;

		private String wf3Pm;

		private String wf4Am;

		private String wf4Pm;

		private String wf5Am;

		private String wf5Pm;

		private String wf6Am;

		private String wf6Pm;

		private String wf7Am;

		private String wf7Pm;

		private String wf8;

		private String wf9;

		private String wf10;
	}
}
