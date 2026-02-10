package com.nextorm.portal.dto.tool;

import lombok.Data;

import java.util.List;

@Data
public class ToolBulkDeleteRequestDto {
	private List<Long> ids;
}
