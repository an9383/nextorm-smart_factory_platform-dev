package com.nextorm.portal.controller.system;

import com.nextorm.portal.dto.system.*;
import com.nextorm.portal.service.system.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * 로그인 된 사용자 정보 조회
	 *
	 * @return
	 */
	@GetMapping("/me")
	public LoginUserResponseDto getLoginUserInfo(
	) {
		return userService.getLoginUser();
	}

	@GetMapping("")
	public List<UserResponseDto> getUserList() {
		return userService.getUserList();
	}

	@GetMapping("/{userId}")
	public UserResponseDto getUser(@PathVariable(name = "userId") Long userId) {
		return userService.getUser(userId);
	}

	@PostMapping
	public UserResponseDto createUser(@RequestBody UserCreateRequestDto userCreateRequestDto) {
		return userService.createUser(userCreateRequestDto);
	}

	@PutMapping("/{userId}")
	public UserResponseDto modifyUser(
		@PathVariable(name = "userId") Long userId,
		@RequestBody UserUpdateRequestDto userUpdateRequestDto
	) {
		return userService.modifyUser(userId, userUpdateRequestDto);
	}

	@PutMapping("/change-password")
	public boolean changePassword(
		@RequestBody UserChangePasswordDto userChangePassword
	) {
		return userService.changePassword(userChangePassword);
	}

	@PutMapping("/change-locale")
	public void changeLocale(@RequestBody UserChangeLocaleDto userChangeLocale) {
		userService.changeLocale(userChangeLocale.getLocale());
	}

	@DeleteMapping("/{userId}")
	public UserResponseDto deleteUser(@PathVariable(name = "userId") Long userId) {
		return userService.deleteUser(userId);
	}

	@PostMapping("/bulk-delete")
	public List<UserResponseDto> deleteUsers(@RequestBody List<Long> userIds) {
		return userService.deleteUsers(userIds);
	}

	@PostMapping("/create-update-setting")
	public UserSettingResponseDto createOrUpdateUserSetting(@RequestBody UserSettingRequestDto dto) {
		return userService.createOrUpdateUserSetting(dto);
	}

	@GetMapping(value = "/{userId}/image")
	public ResponseEntity<String> getUserImage(@PathVariable(name = "userId") String userId) {
		CacheControl cacheControl = CacheControl.maxAge(60, TimeUnit.SECONDS)
												.noTransform()
												.mustRevalidate();
		return ResponseEntity.ok()
							 .body(userService.getUserImage(userId));
	}

	@PostMapping("/{userId}/token")
	public void updateUserToken(@PathVariable(name = "userId") Long userId) {
		userService.updateUserToken(userId);
	}
}
