package com.nextorm.portal.dto.processconfig;

import com.nextorm.common.db.entity.ProcessConfig;
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
public class ProcessConfigResponseDto {
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

	public static ProcessConfigResponseDto from(
		ProcessConfig processConfig,
		List<Tool> innerTools
	) {
		return ProcessConfigResponseDto.builder()
									   .id(processConfig.getId())
									   .name(processConfig.getName())
									   .isUseFailover(processConfig.getIsUseFailover())
									   .systemIp(processConfig.getSystemIp())
									   .connectionTimeout(processConfig.getConnectionTimeout())
									   .hosts(processConfig.getHosts())
									   .tools(innerTools.stream()
														.map(InnerTool::from)
														.toList())
									   .createBy(processConfig.getCreateBy())
									   .createAt(processConfig.getCreateAt())
									   .updateBy(processConfig.getUpdateBy())
									   .updateAt(processConfig.getUpdateAt())
									   .build();
	}
}
