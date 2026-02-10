package com.nextorm.portal.dto.summaryconfig;

import com.nextorm.common.db.entity.SummaryConfig;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.Tool.ToolType;
import com.nextorm.common.db.entity.Tool.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryConfigResponseDto {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	static class InnerTool {
		private Long id;
		private String name;
		private Type type;
		private ToolType toolType;
		private Long parentId;
		private String createBy;
		private LocalDateTime createAt;
		private String updateBy;
		private LocalDateTime updateAt;

		public static InnerTool from(Tool tool) {
			return InnerTool.builder()
							.id(tool.getId())
							.name(tool.getName())
							.type(tool.getType())
							.toolType(tool.getToolType())
							.parentId(tool.getParentId())
							.createAt(tool.getCreateAt())
							.createBy(tool.getCreateBy())
							.updateAt(tool.getUpdateAt())
							.updateBy(tool.getUpdateBy())
							.build();
		}
	}

	private Long id;
	private String name;
	private Boolean isUseFailover;
	private String systemIp;
	private Integer connectionTimeout;
	private String hosts;
	@Builder.Default
	private List<InnerTool> tools = new ArrayList<>();
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static SummaryConfigResponseDto from(
		SummaryConfig summaryConfig,
		List<Tool> innerTools
	) {
		return SummaryConfigResponseDto.builder()
									   .id(summaryConfig.getId())
									   .name(summaryConfig.getName())
									   .isUseFailover(summaryConfig.getIsUseFailover())
									   .systemIp(summaryConfig.getSystemIp())
									   .connectionTimeout(summaryConfig.getConnectionTimeout())
									   .hosts(summaryConfig.getHosts())
									   .tools(innerTools.stream()
														.map(InnerTool::from)
														.toList())
									   .createBy(summaryConfig.getCreateBy())
									   .createAt(summaryConfig.getCreateAt())
									   .updateBy(summaryConfig.getUpdateBy())
									   .updateAt(summaryConfig.getUpdateAt())
									   .build();
	}
}
