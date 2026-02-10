package com.nextorm.portal.dto.location;

import com.nextorm.common.db.entity.Location;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationCreateRequestDto {
	private Location.Type type;
	private String name;
	private String description;
	private Long parentId;
	private Integer seq;
	private Double latitude;
	private Double longitude;

	public Location toEntity() {
		return Location.builder()
					   .type(this.type)
					   .name(this.name)
					   .description(this.description)
					   .seq(this.seq)
					   .latitude(this.latitude)
					   .longitude(this.longitude)
					   .build();
	}
}
