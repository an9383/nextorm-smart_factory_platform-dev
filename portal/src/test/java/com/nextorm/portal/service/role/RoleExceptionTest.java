package com.nextorm.portal.service.role;

import com.nextorm.portal.common.exception.role.RoleErrorCode;
import com.nextorm.portal.common.exception.role.RoleNameDuplicationException;
import com.nextorm.portal.common.exception.role.RoleNotFoundException;
import com.nextorm.portal.dto.system.RoleRequestDto;
import com.nextorm.portal.entity.system.Role;
import com.nextorm.portal.repository.system.RoleRepository;
import com.nextorm.portal.repository.system.UserRepository;
import com.nextorm.portal.service.system.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleExceptionTest {
	@InjectMocks
	RoleService roleService;

	@Mock
	RoleRepository roleRepository;

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@DisplayName("getRole 요청 시 잘못된 RoleId를 요청했을 경우: RoleNotFoundException")
	@Test
	void givenRoleIdThenRoleNotFoundExceptionWhenGetRole() {
		assertThatThrownBy(() -> roleService.getRole(111111L)).isInstanceOf(RoleNotFoundException.class)
															  .hasMessage(RoleErrorCode.ROLE_NOT_FOUND_EXCEPTION.getMessage());
	}

	@DisplayName("createRole 요청 시 RoleName이 중복 될 경우: RoleNameDuplicationException")
	@Test
	void givenRoleNameThenRoleNameDuplicationExceptionWhenCreateRole() {
		Role role = Role.builder()
						.name("test")
						.build();
		when(roleRepository.findByName(role.getName())).thenReturn(Optional.of(role));

		RoleRequestDto request = new RoleRequestDto("test", "test", "test");

		assertThatThrownBy(() -> roleService.createRole(request)).isInstanceOf(RoleNameDuplicationException.class)
																 .hasMessage(RoleErrorCode.ROLE_NAME_DUPLICATION.getMessage());
	}

	@DisplayName("modifyRole 요청 시 잘못된 RoleId가 요청 될 경우: RoleNotFoundException")
	@Test
	void givenRoleIdThenRoleNotFoundExceptionWhenModifyRole() {
		RoleRequestDto request = new RoleRequestDto("test", "test", "test");

		assertThatThrownBy(() -> roleService.modifyRole(111111L, request)).isInstanceOf(RoleNotFoundException.class)
																		  .hasMessage(RoleErrorCode.ROLE_NOT_FOUND_EXCEPTION.getMessage());
	}

	@DisplayName("deleteRole 요청 시 잘못된 RoleId가 요청 될 경우: RoleNotFoundException")
	@Test
	void givenRoleIdThenRoleNotFoundExceptionWhenDeleteRole() {
		assertThatThrownBy(() -> roleService.deleteRole(111111L)).isInstanceOf(RoleNotFoundException.class)
																 .hasMessage(RoleErrorCode.ROLE_NOT_FOUND_EXCEPTION.getMessage());
	}
}
