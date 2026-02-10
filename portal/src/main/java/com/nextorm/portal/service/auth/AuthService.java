package com.nextorm.portal.service.auth;

import com.nextorm.portal.config.security.JwtAuthenticationError;
import com.nextorm.portal.config.security.JwtAuthenticationException;
import com.nextorm.portal.config.security.JwtTokenProvider;
import com.nextorm.portal.config.security.ParsedToken;
import com.nextorm.portal.entity.auth.UserRefreshToken;
import com.nextorm.portal.entity.system.User;
import com.nextorm.portal.repository.auth.UserRefreshTokenRepository;
import com.nextorm.portal.repository.system.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final UserRefreshTokenRepository userRefreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthToken loginProcess(
		String loginId,
		String password
	) {
		User user = userRepository.findByLoginId(loginId)
								  .orElseThrow(() -> new JwtAuthenticationException(JwtAuthenticationError.ID_PASSWORD_INVALID));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new JwtAuthenticationException(JwtAuthenticationError.ID_PASSWORD_INVALID);
		}

		AuthToken authToken = issueAuthenticationToken(loginId, "ROLE_USER");

		return AuthToken.builder()
						.accessToken(authToken.getAccessToken())
						.refreshToken(authToken.getRefreshToken())
						.build();
	}

	public AuthToken tokenLoginProcess(
		String loginId,
		String token
	) {
		User user = userRepository.findByLoginId(loginId)
								  .orElseThrow(() -> new JwtAuthenticationException(JwtAuthenticationError.ID_PASSWORD_INVALID));

		if (!token.equals(user.getToken())) {
			throw new JwtAuthenticationException(JwtAuthenticationError.ID_PASSWORD_INVALID);
		}

		AuthToken authToken = issueAuthenticationToken(loginId, "ROLE_USER");

		return AuthToken.builder()
						.accessToken(authToken.getAccessToken())
						.refreshToken(authToken.getRefreshToken())
						.build();
	}

	/**
	 * Access Token, Refresh Token 발급
	 *
	 * @param loginId
	 * @param role
	 * @return
	 */
	public AuthToken issueAuthenticationToken(
		String loginId,
		String role
	) {
		String accessToken = jwtTokenProvider.generateAccessToken(loginId, role);
		String refreshToken = jwtTokenProvider.generateRefreshToken(loginId, role);
		saveRefreshToken(refreshToken);

		return new AuthToken(accessToken, refreshToken);
	}

	/**
	 * Access Token 만료 시 Access Token, Refresh Token 재발급
	 *
	 * @param oldRefreshToken
	 * @return
	 */
	public AuthToken reissueAuthenticationToken(
		String oldRefreshToken
	) {
		ParsedToken parsedToken = jwtTokenProvider.parseToken(oldRefreshToken);

		// Access Token으로는 재발급 불가
		if (parsedToken.isAccessToken()) {
			throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
		}

		// 기 발급된 refresh token 제거
		UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByToken(oldRefreshToken);
		if (userRefreshToken == null) {
			throw new JwtAuthenticationException(JwtAuthenticationError.TOKEN_INVALID);
		}
		userRefreshTokenRepository.delete(userRefreshToken);
		return this.issueAuthenticationToken(parsedToken.getLoginId(), parsedToken.getRole());
	}

	public void saveRefreshToken(String refreshToken) {
		ParsedToken parsedToken = jwtTokenProvider.parseToken(refreshToken);

		Date expiration = parsedToken.getExpiration();
		Date issuedAt = parsedToken.getIssuedAt();
		UserRefreshToken userRefreshToken = UserRefreshToken.builder()
															.loginId(parsedToken.getLoginId())
															.token(refreshToken)
															.issuedAt(issuedAt.toInstant()
																			  .atZone(ZoneId.systemDefault())
																			  .toLocalDateTime())
															.expiration(expiration.toInstant()
																				  .atZone(ZoneId.systemDefault())
																				  .toLocalDateTime())
															.build();
		userRefreshTokenRepository.save(userRefreshToken);
	}

	/**
	 * 로그아웃 시 refresh token DB에서 제거
	 *
	 * @param refreshToken
	 */
	@Transactional
	public void deleteRefreshToken(String refreshToken) {
		userRefreshTokenRepository.deleteByToken(refreshToken);
	}
}
