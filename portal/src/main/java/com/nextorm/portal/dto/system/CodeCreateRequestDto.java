package com.nextorm.portal.dto.system;

import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.entity.system.code.Code.CodeBuilder;
import com.nextorm.common.db.entity.system.code.CodeCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeCreateRequestDto {
	private Long categoryId;
	private String code;
	private String name;
	private String value;
	private String description;
	private Long childCategoryId;
	private List<Long> childCodeIds = List.of();

	public Code toEntity(
		CodeCategory category,
		CodeCategory childCategory,
		List<Code> childCodes,
		int sort
	) {
		CodeBuilder builder = Code.builder()
								  .category(category)
								  .code(code)
								  .name(name)
								  .value(value)
								  .description(description)
								  .sort(sort);
		if (childCategoryId != null) {
			builder.childCategory(childCategory);
		}

		Code code = builder.build();

		if (childCodes != null) {
			childCodes.forEach(childCode -> code.addChildCode(childCode));
		}

		return code;
	}
}
