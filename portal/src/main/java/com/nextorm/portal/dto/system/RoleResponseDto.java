package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {
	private Long id;
	private String name;
	private String description;
	private String permission;

	public static RoleResponseDto from(Role entity) {
		if (entity == null) {
			return null;
		}
		return RoleResponseDto.builder()
							  .id(entity.getId())
							  .name(entity.getName())
							  .description(entity.getDescription())
							  .permission(entity.getPermission())
							  .build();
	}
}