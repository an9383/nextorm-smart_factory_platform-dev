package com.nextorm.portal.dto.system;

import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.entity.system.code.CodeHierarchy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CodeResponseDto {
	private Long id;
	private Long categoryId;
	private String category;
	private String categoryName;
	private String name;
	private String code;
	private String value;
	private Integer sort;
	private String description;
	private Long childCategoryId;
	private String childCategory;
	private List<CodeListResponseDto> childCodes;
	private List<CodeListResponseDto> parentCodes;
	private LocalDateTime createAt;
	private String createBy;
	private LocalDateTime updateAt;
	private String updateBy;

	public static CodeResponseDto from(Code entity) {
		CodeResponseDtoBuilder builder = CodeResponseDto.builder()
														.id(entity.getId())
														.categoryId(entity.getCategory()
																		  .getId())
														.category(entity.getCategory()
																		.getCategory())
														.categoryName(entity.getCategory()
																			.getName())
														.name(entity.getName())
														.code(entity.getCode())
														.value(entity.getValue())
														.sort(entity.getSort())
														.description(entity.getDescription())
														.parentCodes(CodeListResponseDto.from(entity.getParentCodes()
																									.stream()
																									.map(CodeHierarchy::getParent)
																									.toList()))
														.childCodes(CodeListResponseDto.from(entity.getChildCodes()
																								   .stream()
																								   .map(CodeHierarchy::getChild)
																								   .toList()))
														.createAt(entity.getCreateAt())
														.createBy(entity.getCreateBy())
														.updateAt(entity.getUpdateAt())
														.updateBy(entity.getUpdateBy());
		if (entity.getChildCategory() != null) {
			builder.childCategoryId(entity.getChildCategory()
										  .getId())
				   .childCategory(entity.getChildCategory()
										.getCategory());
		}

		return builder.build();
	}
}
