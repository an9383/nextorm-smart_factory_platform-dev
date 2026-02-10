package com.nextorm.extensions.proxystatusgateway.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 서버 설정 관리를 위한 Configuration 클래스
 * JSON 파일에서 서버 목록을 읽어와 검증 후 빈으로 등록
 */
@Configuration
@Slf4j
public class ServerConfiguration {

	@Value("${proxy.status.servers.config-path:servers.json}")
	private String serverConfigPath;

	private final ObjectMapper objectMapper;
	private final Validator validator;

	public ServerConfiguration(
		ObjectMapper objectMapper,
		Validator validator
	) {
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	/**
	 * JSON 파일에서 서버 목록을 읽어와 검증 후 반환
	 */
	@Bean("serverList")
	public List<ServerInfo> loadServerList() throws IOException {
		log.info("서버 설정 파일 로드 시작: {}", serverConfigPath);

		Resource resource = getConfigResource();

		if (!resource.exists()) {
			throw new IOException("서버 설정 파일을 찾을 수 없습니다: " + serverConfigPath);
		}

		List<ServerInfo> servers = objectMapper.readValue(resource.getInputStream(),
			new TypeReference<List<ServerInfo>>() {
			});

		validateServers(servers);

		log.info("서버 설정 로드 완료: {}개 서버", servers.size());
		servers.forEach(server -> log.debug("로드된 서버: {}", server));

		return servers;
	}

	/**
	 * 설정 파일 경로에 따라 적절한 Resource 반환
	 */
	private Resource getConfigResource() {
		if (serverConfigPath.startsWith("classpath:")) {
			return new ClassPathResource(serverConfigPath.substring("classpath:".length()));
		} else if (serverConfigPath.startsWith("/") || serverConfigPath.contains(":")) {
			// 절대 경로
			return new FileSystemResource(serverConfigPath);
		} else {
			// 상대 경로 - 먼저 classpath에서 찾고, 없으면 파일 시스템에서 찾기
			ClassPathResource classPathResource = new ClassPathResource(serverConfigPath);
			if (classPathResource.exists()) {
				return classPathResource;
			} else {
				return new FileSystemResource(serverConfigPath);
			}
		}
	}

	/**
	 * 서버 정보 검증
	 */
	private void validateServers(List<ServerInfo> servers) {
		if (servers == null || servers.isEmpty()) {
			throw new IllegalArgumentException("서버 목록이 비어있습니다");
		}

		for (int i = 0; i < servers.size(); i++) {
			ServerInfo server = servers.get(i);
			Set<ConstraintViolation<ServerInfo>> violations = validator.validate(server);

			if (!violations.isEmpty()) {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("서버 정보 검증 실패 (인덱스: ")
						.append(i)
						.append("): ");

				for (ConstraintViolation<ServerInfo> violation : violations) {
					errorMsg.append(violation.getMessage())
							.append("; ");
				}

				throw new IllegalArgumentException(errorMsg.toString());
			}
		}

		// 중복 이름 체크
		long uniqueNames = servers.stream()
								  .map(ServerInfo::getName)
								  .distinct()
								  .count();

		if (uniqueNames != servers.size()) {
			throw new IllegalArgumentException("서버 이름에 중복이 있습니다");
		}
	}
}
