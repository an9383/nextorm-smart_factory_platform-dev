package com.nextorm.portal.dto.system;

import com.nextorm.portal.entity.system.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {
	private String name;
	private String description;
	private String permission;

	public Role toEntity() {
		return Role.builder()
				   .name(name)
				   .description(description)
				   .permission(permission)
				   .build();
	}
}