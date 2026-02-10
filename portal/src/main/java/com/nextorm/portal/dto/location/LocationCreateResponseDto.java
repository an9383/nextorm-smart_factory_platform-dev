package com.nextorm.portal.dto.location;

import com.nextorm.common.db.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LocationCreateResponseDto {
	private Long id;
	private Location.Type type;
	private String name;
	private String description;
	private Long parentId;
	private Integer seq;
	private Double latitude;
	private Double longitude;

	public static LocationCreateResponseDto of(
		Location location,
		Location parent
	) {
		Long parentId = parent == null
						? null
						: parent.getId();

		return LocationCreateResponseDto.builder()
										.id(location.getId())
										.type(location.getType())
										.name(location.getName())
										.description(location.getDescription())
										.parentId(parentId)
										.seq(location.getSeq())
										.latitude(location.getLatitude())
										.longitude(location.getLongitude())
										.build();
	}
}
