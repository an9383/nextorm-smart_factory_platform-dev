package com.nextorm.portal.dto.location;

import com.nextorm.common.db.entity.Location;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationModifyRequestDto {
	private String name;
	private String description;
	private Integer seq;
	private Double latitude;
	private Double longitude;

	public Location toEntity() {
		return Location.builder()
					   .name(this.name)
					   .description(this.description)
					   .seq(this.seq)
					   .latitude(this.latitude)
					   .longitude(this.longitude)
					   .build();
	}
}
