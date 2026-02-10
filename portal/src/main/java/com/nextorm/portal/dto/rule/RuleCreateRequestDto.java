package com.nextorm.portal.dto.rule;

import com.nextorm.common.db.entity.Rule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCreateRequestDto {
	private String name;
	private String className;
	private String description;
	private Long dcpConfigId;

	public Rule toEntity() {
		return Rule.builder()
				   .name(name)
				   .className(className)
				   .description(description)
				   .build();
	}

	public static RuleCreateRequestDto from(Rule entity) {
		if (entity == null) {
			return null;
		}
		return RuleCreateRequestDto.builder()
								   .name(entity.getName())
								   .className(entity.getClassName())
								   .description(entity.getDescription())
								   .build();
	}
}
