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
public class RuleUpdateRequestDto {
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
}
