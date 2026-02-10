package com.nextorm.extensions.proxystatusgateway.exception;

import lombok.Getter;

/**
 * 서버를 찾을 수 없을 때 발생하는 예외
 */
@Getter
public class ServerNotFoundException extends RuntimeException {
	private final String serverName;

	public ServerNotFoundException(String serverName) {
		super("서버 '" + serverName + "'를 찾을 수 없습니다");
		this.serverName = serverName;
	}
}
