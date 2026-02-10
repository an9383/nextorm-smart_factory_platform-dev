package com.nextorm.portal.dto.system;

import com.nextorm.common.db.entity.system.code.CodeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CodeCategoryResponseDto {
	private Long id;
	private String category;
	private String name;
	private String description;
	private LocalDateTime createAt;
	private String createBy;
	private LocalDateTime updateAt;
	private String updateBy;

	public static CodeCategoryResponseDto from(CodeCategory entity) {
		return CodeCategoryResponseDto.builder()
									  .id(entity.getId())
									  .category(entity.getCategory())
									  .name(entity.getName())
									  .description(entity.getDescription())
									  .createAt(entity.getCreateAt())
									  .createBy(entity.getCreateBy())
									  .updateAt(entity.getUpdateAt())
									  .updateBy(entity.getUpdateBy())
									  .build();
	}
}
