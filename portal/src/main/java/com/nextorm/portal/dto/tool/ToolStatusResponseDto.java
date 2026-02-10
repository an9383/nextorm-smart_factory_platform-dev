package com.nextorm.portal.dto.tool;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.ToolStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ToolStatusResponseDto {
	private Long toolId;
	private String toolName;
	private LocalDateTime toolStatusDate;
	private int leadTime;
	private Double operationRate;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ToolStatusResponseDto from(ToolStatus entity) {
		if (entity == null) {
			return null;
		}

		Tool tool = entity.getTool();

		return ToolStatusResponseDto.builder()
									.toolId(tool != null
											? tool.getId()
											: null)
									.toolName(tool != null
											  ? tool.getName()
											  : null)
									.toolStatusDate(entity.getToolStatusDate())
									.leadTime(entity.getLeadTime())
									.operationRate(entity.getOperationRate())
									.createAt(entity.getCreateAt())
									.createBy(entity.getCreateBy())
									.updateAt(entity.getUpdateAt())
									.updateBy(entity.getUpdateBy())
									.build();
	}

}
