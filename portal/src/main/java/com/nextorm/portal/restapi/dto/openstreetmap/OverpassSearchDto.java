package com.nextorm.portal.restapi.dto.openstreetmap;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OverpassSearchDto {
	private List<Element> elements = new ArrayList<>();

	@Data
	public static class Element {
		private long id;
		private double lat;
		private double lon;
		private Tags tags;
		private String type;
		private List<Geometry> geometry;
	}

	@Data
	public static class Tags {
		private String amenity;
		private String landuse;
		private String name;
		private String ncat;
		private String source;
	}

	@Data
	public static class Geometry {
		private double lat;
		private double lon;
	}

}
