package com.nextorm.portal.dto.reservoirLayout;

import com.nextorm.common.db.entity.ReservoirLayout;
import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservoirLayoutUpdateRequestDto {
	private Tool tool;
	private String data;

	public ReservoirLayout toEntity() {
		return ReservoirLayout.builder()
							  .data(data)
							  .build();
	}
}
