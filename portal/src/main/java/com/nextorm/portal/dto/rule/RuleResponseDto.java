package com.nextorm.portal.dto.rule;

import com.nextorm.common.db.entity.Rule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResponseDto {
	private String name;
	private String className;
	private String description;
	private Long dcpConfigId;

	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static RuleResponseDto from(Rule entity) {
		if (entity == null) {
			return null;
		}
		return RuleResponseDto.builder()
							  .name(entity.getName())
							  .className(entity.getClassName())
							  .description(entity.getDescription())
							  .id(entity.getId())
							  .createBy(entity.getCreateBy())
							  .createAt(entity.getCreateAt())
							  .updateBy(entity.getUpdateBy())
							  .updateAt(entity.getUpdateAt())
							  .build();
	}
}
