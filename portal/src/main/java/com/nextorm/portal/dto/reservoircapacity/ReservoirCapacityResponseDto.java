package com.nextorm.portal.dto.reservoircapacity;

import com.nextorm.common.db.entity.ReservoirCapacity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservoirCapacityResponseDto {
	private Long locationId;
	private String locationName;
	private Double reservoirCapacity;
	private Double rainFall;
	private LocalDateTime date;
	private String description;

	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ReservoirCapacityResponseDto from(ReservoirCapacity entity) {
		if (entity == null) {
			return null;
		}
		return ReservoirCapacityResponseDto.builder()
										   .locationId(entity.getLocation() != null
													   ? entity.getLocation()
															   .getId()
													   : null)
										   .locationName(entity.getLocation() != null
														 ? entity.getLocation()
																 .getName()
														 : null)
										   .reservoirCapacity(entity.getReservoirCapacity())
										   .rainFall(entity.getRainFall())
										   .date(entity.getDate())
										   .description(entity.getDescription())
										   .id(entity.getId())
										   .createBy(entity.getCreateBy())
										   .createAt(entity.getCreateAt())
										   .updateBy(entity.getUpdateBy())
										   .updateAt(entity.getUpdateAt())
										   .build();
	}
}
