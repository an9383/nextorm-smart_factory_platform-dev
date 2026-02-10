package com.nextorm.portal.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ToolCollectStatusResponseDto {
	private Long toolId;
	private String toolName;
	private List<Long> dcpIds;
	private Integer dcpGoodCnt;
	private Integer dcpBadCnt;
	private Integer parameterGoodCnt;
	private Integer parameterBadCnt;
	private List<LocalDateTime> lastCollectedAtList;
	private List<Long> goodCollectedParameterIds;
	private List<Long> badCollectedParameterIds;
	private List<String> goodCollectedParameterNames;
	private List<String> badCollectedParameterNames;
}
