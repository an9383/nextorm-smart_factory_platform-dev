package com.nextorm.portal.dto.location;

import com.nextorm.common.db.entity.Location;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LocationDto {
	private Long id;
	private Location.Type type;
	private String name;
	private String description;
	private LocationDto parent;
	private Integer seq;
	private Double latitude;
	private Double longitude;

	public static LocationDto of(Location location) {
		if (location != null) {
			return LocationDto.builder()
							  .id(location.getId())
							  .type(location.getType() != null
									? Location.Type.valueOf(location.getType()
																	.toString())
									: null)
							  .name(location.getName())
							  .description(location.getDescription())
							  .parent(location.getParent() != null
									  ? LocationDto.of(location.getParent())
									  : null)
							  .seq(location.getSeq())
							  .latitude(location.getLatitude())
							  .longitude(location.getLongitude())
							  .build();
		} else {
			return null;
		}
	}

	public Location toEntity() {
		return Location.builder()
					   .type(this.type != null
							 ? Location.Type.valueOf(this.type.toString())
							 : null)
					   .name(this.name)
					   .description(this.description)
					   .parent(this.parent != null
							   ? this.parent.toEntity()
							   : null)
					   .seq(this.seq)
					   .latitude(this.latitude)
					   .longitude(this.longitude)
					   .build();
	}
}
