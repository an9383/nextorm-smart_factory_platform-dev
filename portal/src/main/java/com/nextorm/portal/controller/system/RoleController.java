package com.nextorm.portal.controller.system;

import com.nextorm.portal.dto.system.RoleRequestDto;
import com.nextorm.portal.dto.system.RoleResponseDto;
import com.nextorm.portal.service.system.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;

	@GetMapping("")
	public List<RoleResponseDto> getRoles() {
		return roleService.getRoles();
	}

	@GetMapping("/{id}")
	public RoleResponseDto getRole(@PathVariable(name = "id") Long id) {
		return roleService.getRole(id);
	}

	@PostMapping
	public RoleResponseDto createRole(@RequestBody RoleRequestDto roleRequest) {
		return roleService.createRole(roleRequest);
	}

	@PutMapping("/{id}")
	public RoleResponseDto modifyRole(
		@PathVariable(name = "id") Long id,
		@RequestBody RoleRequestDto roleRequest
	) {
		return roleService.modifyRole(id, roleRequest);
	}

	@DeleteMapping("/{id}")
	public RoleResponseDto deleteRole(@PathVariable(name = "id") Long id) {
		return roleService.deleteRole(id);
	}

	@PostMapping("/bulk-delete")
	public List<RoleResponseDto> deleteRole(@RequestBody List<Long> ids) {
		return roleService.deleteRoles(ids);
	}

}
