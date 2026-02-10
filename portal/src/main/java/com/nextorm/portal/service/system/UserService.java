package com.nextorm.portal.service.system;

import com.nextorm.portal.common.exception.user.UserIdDuplicationException;
import com.nextorm.portal.common.exception.user.UserNotFoundException;
import com.nextorm.portal.config.security.JwtAuthenticationError;
import com.nextorm.portal.config.security.JwtAuthenticationException;
import com.nextorm.portal.dto.system.*;
import com.nextorm.portal.entity.system.Role;
import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.entity.system.UserSetting;
import com.nextorm.portal.repository.system.RoleRepository;
import com.nextorm.portal.repository.system.UserRepository;
import com.nextorm.portal.repository.system.UserSettingRepository;
import com.nextorm.portal.service.auth.SessionService;
import com.nextorm.portal.util.ImageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final SessionService sessionService;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserSettingRepository userSettingRepository;

	@Transactional
	public LoginUserResponseDto getLoginUser(
	) {
		String loginId = sessionService.getLoginId();
		User user = userRepository.findByLoginId(loginId)
								  .orElseThrow(() -> new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID));

		return LoginUserResponseDto.from(user);
	}

	public List<UserResponseDto> getUserList() {
		return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createAt"))
							 .stream()
							 .map(UserResponseDto::from)
							 .toList();
	}

	public UserResponseDto getUser(Long userId) {
		User user = userRepository.findById(userId)
								  .orElseThrow(UserNotFoundException::new);
		return UserResponseDto.from(user);
	}

	public boolean changePassword(
		UserChangePasswordDto userChangePasswordDto
	) {
		String loginId = sessionService.getLoginId();
		User user = userRepository.findByLoginId(loginId)
								  .orElseThrow(() -> new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID));
		if (passwordEncoder.matches(userChangePasswordDto.getCurrentPassword(), user.getPassword())) {
			String password = passwordEncoder.encode(userChangePasswordDto.getChangePassword());
			user.modifyPassword(password);
			return true;
		} else {
			return false;
		}
	}

	public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
		//중복된 로그인 아이디가 있느지 확인
		userRepository.findByLoginId(userCreateRequestDto.getLoginId())
					  .ifPresent(user -> {
						  throw new UserIdDuplicationException(userCreateRequestDto.getLoginId());
					  });

		userCreateRequestDto.setPassword(passwordEncoder.encode(userCreateRequestDto.getPassword()));
		List<Role> roles = new ArrayList<>();
		if (userCreateRequestDto.getRoleIds() != null && !userCreateRequestDto.getRoleIds()
																			  .isEmpty()) {
			roles = roleRepository.findAllById(userCreateRequestDto.getRoleIds());
		}

		byte[] resizedImage = null;
		if (userCreateRequestDto.getImage() != null) {
			resizedImage = resizeImage(userCreateRequestDto.getImage());
		}
		String userToken = makeUserToken();
		User user = userCreateRequestDto.toEntity(roles, resizedImage, userToken);
		userRepository.save(user);
		return UserResponseDto.from(user);
	}

	private String makeUserToken() {
		return UUID.randomUUID()
				   .toString()
				   .replaceAll("-", "");
	}

	private byte[] resizeImage(String base64Image) {
		return ImageUtil.resizeBase64Image(base64Image, 300, 0.75f);
	}

	public UserResponseDto modifyUser(
		Long userId,
		UserUpdateRequestDto userUpdateRequestDto
	) {
		User user = userRepository.findById(userId)
								  .orElseThrow(UserNotFoundException::new);

		String password = null;

		if (userUpdateRequestDto.getPassword() != null && !userUpdateRequestDto.getPassword()
																			   .isEmpty()) {
			password = passwordEncoder.encode(userUpdateRequestDto.getPassword());
		}

		List<Role> roles = new ArrayList<>();
		if (userUpdateRequestDto.getRoleIds() != null) {
			roles = roleRepository.findAllById(userUpdateRequestDto.getRoleIds());
		}
		byte[] newImage = null;

		if (userUpdateRequestDto.getImage() != null && !userUpdateRequestDto.getImage()
																			.isEmpty()) {
			newImage = resizeImage(userUpdateRequestDto.getImage());
		}

		user.modify(password,
			userUpdateRequestDto.getName(),
			userUpdateRequestDto.getEmail(),
			userUpdateRequestDto.getPhone(),
			newImage,
			roles);
		return UserResponseDto.from(user);
	}

	public UserResponseDto deleteUser(Long userId) {
		User user = userRepository.findById(userId)
								  .orElseThrow(UserNotFoundException::new);
		userRepository.delete(user);
		return UserResponseDto.from(user);
	}

	public List<UserResponseDto> deleteUsers(List<Long> userIds) {
		List<User> users = userRepository.findAllById(userIds);
		userRepository.deleteAll(users);
		return users.stream()
					.map(UserResponseDto::from)
					.toList();
	}

	public void changeLocale(Locale locale) {
		String loginId = sessionService.getLoginId();
		userRepository.findByLoginId(loginId)
					  .ifPresentOrElse(user -> user.changeLocale(locale), () -> {
						  throw new UserNotFoundException();
					  });
	}

	@Transactional
	public UserSettingResponseDto createOrUpdateUserSetting(UserSettingRequestDto dto) {
		String loginId = sessionService.getLoginId();
		User user = userRepository.findByLoginId(loginId)
								  .orElseThrow(() -> new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID));
		UserSetting userSetting = userSettingRepository.findByUser(user)
													   .orElse(new UserSetting());
		userSetting.createOrModify(dto, user);

		userSettingRepository.save(userSetting);

		return UserSettingResponseDto.from(userSetting);
	}

	public String getUserImage(String userId) {

		User user = userRepository.findByLoginId(userId)
								  .orElseThrow(UserNotFoundException::new);
		return user.getImage();
	}

	public void updateUserToken(Long userId) {
		User user = userRepository.findById(userId)
								  .orElseThrow(UserNotFoundException::new);
		String userToken = makeUserToken();
		user.updateToken(userToken);
	}
}
