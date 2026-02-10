package com.nextorm.extensions.scheduler.task.modbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TcpModbusConnectionFactory 테스트 클래스
 * <p>
 * 이 클래스는 TcpModbusConnectionFactory의 createConnection 메서드가 올바른 인스턴스를 반환하는지 확인합니다.
 * 생성된 커넥션이 정상 연결되었는지는, 커넥션 객체의 connect() 시점에 확인하므로 테스트 대상이 아님
 * </p>
 */
@DisplayName("TcpModbusConnectionFactory 테스트")
class TcpModbusConnectionFactoryTest {

	private TcpModbusConnectionFactory factory;

	@BeforeEach
	void setUp() {
		factory = new TcpModbusConnectionFactory();
	}

	@Test
	@DisplayName("생성된 객체가 TcpModbusConnection 인스턴스인지 확인")
	void createConnection_정상_파라미터_성공() throws Exception {
		// Given
		String host = "192.168.1.100";
		int port = 502;

		// When
		ModbusConnection connection = factory.createConnection(host, port);

		// Then
		assertNotNull(connection, "연결 객체가 null이면 안됩니다");
		assertInstanceOf(TcpModbusConnection.class, connection, "TcpModbusConnection 인스턴스여야 합니다");
	}
}