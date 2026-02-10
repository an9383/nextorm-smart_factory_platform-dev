package com.nextorm.portal.service.user;

import com.nextorm.portal.common.exception.user.UserErrorCode;
import com.nextorm.portal.common.exception.user.UserIdDuplicationException;
import com.nextorm.portal.common.exception.user.UserNotFoundException;
import com.nextorm.portal.dto.system.UserCreateRequestDto;
import com.nextorm.portal.dto.system.UserUpdateRequestDto;
import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.repository.system.RoleRepository;
import com.nextorm.portal.repository.system.UserRepository;
import com.nextorm.portal.repository.system.UserSettingRepository;
import com.nextorm.portal.service.auth.SessionService;
import com.nextorm.portal.service.system.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserExceptionTest {
	@InjectMocks
	UserService userService;

	@Mock
	SessionService sessionService;

	@Mock
	UserRepository userRepository;

	@Mock
	RoleRepository roleRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	UserSettingRepository userSettingRepository;
	
	@DisplayName("getUser 요청 시 잘못된 UserId를 요청했을 경우: UserNotFoundException")
	@Test
	void givenUserIdThenUserNotFoundExceptionWhenGetUser() {
		assertThatThrownBy(() -> userService.getUser(11111111L)).isInstanceOf(UserNotFoundException.class)
																.hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("createUser 요청 시 중복된 LoginId를 요청했을 경우: UserIdDuplicationException")
	@Test
	void givenUserIdThenUserIdDuplicationExceptionWhenCreateUser() {
		User user = new User(null, null, "test", passwordEncoder.encode("test"), "test", "test", "test", null, null);
		when(userRepository.findByLoginId(user.getLoginId())).thenReturn(Optional.of(user));

		UserCreateRequestDto request = new UserCreateRequestDto();
		request.setLoginId("test");

		assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(UserIdDuplicationException.class)
																 .hasMessage(UserErrorCode.USER_ID_DUPLICATION.getMessage());
	}

	@DisplayName("modifyUser 요청 시 잘못된 UserId를 요청했을 경우: UserNotFoundException")
	@Test
	void givenUserIdThenUserNotFoundExceptionWhenModifyUser() {
		UserUpdateRequestDto request = new UserUpdateRequestDto();
		request.setName("test");

		assertThatThrownBy(() -> userService.modifyUser(111111L, request)).isInstanceOf(UserNotFoundException.class)
																		  .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("deleteUser 요청 시 잘못된 UserId를 요청했을 경우: deleteUser")
	@Test
	void givenUserIdThenUserNotFoundExceptionWhenDeleteUser() {
		assertThatThrownBy(() -> userService.deleteUser(11111111L)).isInstanceOf(UserNotFoundException.class)
																   .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("changeLocale 요청 시 잘못된 UserId를 요청했을 경우: changeLocale")
	@Test
	void givenUserIdThenUserNotFoundExceptionWhenChangeLocale() {
		Locale locale = new Locale("en");
		assertThatThrownBy(() -> userService.changeLocale(locale)).isInstanceOf(UserNotFoundException.class)
																  .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("getUserImage 요청 시 잘못된 UserId를 요청했을 경우")
	@Test
	void givenUserIdThenUserNotFoundExceptionWhenGetUserImage() {
		assertThatThrownBy(() -> userService.getUserImage("testtest")).isInstanceOf(UserNotFoundException.class)
																	  .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
	}
}
