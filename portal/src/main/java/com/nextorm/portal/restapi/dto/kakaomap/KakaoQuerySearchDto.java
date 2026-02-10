package com.nextorm.portal.restapi.dto.kakaomap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoQuerySearchDto {
	private Meta meta;
	private Documents[] documents;

	@Data

	public static class Meta {
		@JsonProperty("total_count")
		private int totalCount;
		@JsonProperty("pageable_count")
		private int pageableCount;
		@JsonProperty("is_end")
		private boolean isEnd;
	}

	@Data
	public static class Documents {
		private String id;
		@JsonProperty("place_name")
		private String placeName;
		@JsonProperty("category_name")
		private String categoryName;
		@JsonProperty("category_group_code")
		private String categoryGroupCode;
		@JsonProperty("category_group_name")
		private String categoryGroupName;
		private String x;
		private String y;
	}

}
