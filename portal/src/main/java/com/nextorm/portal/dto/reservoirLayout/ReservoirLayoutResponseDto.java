package com.nextorm.portal.dto.reservoirLayout;

import com.nextorm.common.db.entity.ReservoirLayout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservoirLayoutResponseDto {
	private Long id;
	private String data;
	private Long toolId;

	public static ReservoirLayoutResponseDto from(ReservoirLayout reservoirLayout) {
		return ReservoirLayoutResponseDto.builder()
										 .id(reservoirLayout.getId())
										 .data(reservoirLayout.getData())
										 .toolId(reservoirLayout.getTool()
																.getId())
										 .build();
	}

	public static List<ReservoirLayoutResponseDto> from(List<ReservoirLayout> reservoirLayouts) {
		return reservoirLayouts.stream()
							   .map(ReservoirLayoutResponseDto::from)
							   .collect(Collectors.toList());
	}

}
