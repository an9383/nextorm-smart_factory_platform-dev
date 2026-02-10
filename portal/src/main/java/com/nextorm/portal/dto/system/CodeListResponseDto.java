package com.nextorm.portal.dto.system;

import com.nextorm.common.db.entity.system.code.Code;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CodeListResponseDto {
	private Long id;
	private Long categoryId;
	private String category;
	private String categoryName;
	private String name;
	private String code;
	private String value;
	private Integer sort;
	private String description;
	private LocalDateTime createAt;
	private String createBy;
	private LocalDateTime updateAt;
	private String updateBy;

	public static CodeListResponseDto from(Code entity) {
		CodeListResponseDtoBuilder builder = CodeListResponseDto.builder()
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
																.createAt(entity.getCreateAt())
																.createBy(entity.getCreateBy())
																.updateAt(entity.getUpdateAt())
																.updateBy(entity.getUpdateBy());
		return builder.build();
	}

	public static List<CodeListResponseDto> from(List<Code> entities) {
		return entities.stream()
					   .map(CodeListResponseDto::from)
					   .toList();
	}
}
