package com.nextorm.portal.dto.reservoirLayout;

import com.nextorm.common.db.entity.ReservoirLayout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservoirLayoutUpdateResponseDto {
	private Long id;
	private String data;
	private String createBy;
	private String updateBy;
	private LocalDateTime createAt;
	private LocalDateTime updateAt;

	public static ReservoirLayoutUpdateResponseDto from(ReservoirLayout reservoirLayout) {
		return ReservoirLayoutUpdateResponseDto.builder()
											   .id(reservoirLayout.getId())
											   .data(reservoirLayout.getData())
											   .build();
	}
}
