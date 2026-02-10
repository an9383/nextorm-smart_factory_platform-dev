package com.nextorm.portal.dto.collectorconfig;

import com.nextorm.common.db.entity.CollectorConfig;
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
public class CollectorConfigResponseDto {
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

	public static CollectorConfigResponseDto from(
		CollectorConfig collectorConfig,
		List<Tool> innerTools
	) {
		return CollectorConfigResponseDto.builder()
										 .id(collectorConfig.getId())
										 .name(collectorConfig.getName())
										 .isUseFailover(collectorConfig.isUseFailover())
										 .systemIp(collectorConfig.getSystemIp())
										 .connectionTimeout(collectorConfig.getConnectionTimeout())
										 .hosts(collectorConfig.getHosts())
										 .tools(innerTools.stream()
														  .map(InnerTool::from)
														  .toList())
										 .createBy(collectorConfig.getCreateBy())
										 .createAt(collectorConfig.getCreateAt())
										 .updateBy(collectorConfig.getUpdateBy())
										 .updateAt(collectorConfig.getUpdateAt())
										 .build();
	}
}
