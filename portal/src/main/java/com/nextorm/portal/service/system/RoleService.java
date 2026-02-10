package com.nextorm.portal.service.system;

import com.nextorm.portal.common.exception.role.RoleInUseException;
import com.nextorm.portal.common.exception.role.RoleNameDuplicationException;
import com.nextorm.portal.common.exception.role.RoleNotFoundException;
import com.nextorm.portal.dto.system.RoleRequestDto;
import com.nextorm.portal.dto.system.RoleResponseDto;
import com.nextorm.portal.entity.system.Role;
import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.repository.system.RoleRepository;
import com.nextorm.portal.repository.system.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoleService {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;

	public List<RoleResponseDto> getRoles() {
		return roleRepository.findAll()
							 .stream()
							 .map(RoleResponseDto::from)
							 .toList();
	}

	public RoleResponseDto getRole(Long id) {
		Role role = roleRepository.findById(id)
								  .orElseThrow(RoleNotFoundException::new);
		return RoleResponseDto.from(role);
	}

	@Transactional
	public RoleResponseDto createRole(RoleRequestDto roleRequest) {

		//Role 이름 중복체크
		roleRepository.findByName(roleRequest.getName())
					  .ifPresent(it -> {
						  throw new RoleNameDuplicationException(roleRequest.getName());
					  });

		Role role = roleRequest.toEntity();
		roleRepository.save(role);

		return RoleResponseDto.from(role);
	}

	@Transactional
	public RoleResponseDto modifyRole(
		Long id,
		RoleRequestDto roleRequest
	) {

		Role role = roleRepository.findById(id)
								  .orElseThrow(RoleNotFoundException::new);

		if (!role.getName()
				 .equals(roleRequest.getName())) {
			//Role 이름 중복체크
			roleRepository.findByName(roleRequest.getName())
						  .ifPresent(it -> {
							  throw new RoleNameDuplicationException(roleRequest.getName());
						  });
		}
		role.modify(roleRequest.getName(), roleRequest.getDescription(), roleRequest.getPermission());
		return RoleResponseDto.from(role);
	}

	@Transactional
	public RoleResponseDto deleteRole(Long id) {
		Role role = roleRepository.findById(id)
								  .orElseThrow(RoleNotFoundException::new);

		List<User> roleUsers = userRepository.findByUserRolesRoleId(id);
		if (!roleUsers.isEmpty()) {
			throw new RoleInUseException();
		}
		roleRepository.delete(role);

		return RoleResponseDto.from(role);
	}

	@Transactional
	public List<RoleResponseDto> deleteRoles(List<Long> ids) {
		return ids.stream()
				  .map(this::deleteRole)
				  .toList();
	}
}
