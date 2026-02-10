package com.nextorm.portal.dto.reservoirLayout;

import com.nextorm.common.db.entity.ReservoirLayout;
import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservoirLayoutCreateRequestDto {
	private Tool tool;
	private String data;

	public ReservoirLayout toEntity(Tool tool) {
		return ReservoirLayout.builder()
							  .tool(tool)
							  .data(data)
							  .build();
	}
}
