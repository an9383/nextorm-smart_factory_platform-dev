package com.nextorm.extensions.scheduler.task.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.SimpleDigitalOut;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TcpModbusConnection 통합 테스트 (인메모리 Modbus 서버 사용)")
class TcpModbusConnectionTest {

	private static final String TEST_HOST = "localhost";
	private int testPort; // 동적으로 할당될 포트

	private ModbusSlave modbusServer;
	private TcpModbusConnection tcpModbusConnection;
	private SimpleProcessImage processImage;

	@BeforeEach
	void setUp() throws Exception {
		// 사용 가능한 포트 찾기
		testPort = findAvailablePort();

		// 인메모리 Modbus 서버 설정
		setupModbusServer();

		// 테스트 대상 객체 초기화
		tcpModbusConnection = new TcpModbusConnection(TEST_HOST, testPort);

		// 서버 시작 후 잠시 대기
		Thread.sleep(100);
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		// 클라이언트 연결 종료
		if (tcpModbusConnection != null) {
			tcpModbusConnection.close();
		}

		// 서버 종료
		if (modbusServer != null) {
			modbusServer.close();
			// 서버가 완전히 종료될 때까지 잠시 대기
			Thread.sleep(100);
		}
	}

	@Test
	@DisplayName("실제 Modbus 서버와 연결 성공 테스트")
	void connect_실제_서버_연결_성공() throws IOException {
		// When
		tcpModbusConnection.connect();

		// Then
		assertTrue(tcpModbusConnection.isConnected());
	}

	@Test
	@DisplayName("실제 Modbus 서버에 코일 쓰기 성공 테스트")
	void executeCoilWrite_실제_서버_쓰기_성공() throws IOException, ModbusException {
		// Given
		tcpModbusConnection.connect();
		int address = 0;
		boolean value = true;
		int unitId = 1;

		// When
		tcpModbusConnection.executeCoilWrite(address, value, unitId);

		// Then
		// 프로세스 이미지에서 값이 올바르게 설정되었는지 확인
		SimpleDigitalOut digitalOut = (SimpleDigitalOut)processImage.getDigitalOut(address);
		assertEquals(value, digitalOut.isSet());
	}

	@Test
	@DisplayName("여러 코일에 연속으로 쓰기 테스트")
	void executeCoilWrite_여러_코일_연속_쓰기() throws IOException, ModbusException {
		// Given
		tcpModbusConnection.connect();
		int unitId = 1;

		// When & Then
		for (int address = 0; address < 5; address++) {
			boolean value = address % 2 == 0; // 짝수 주소는 true, 홀수 주소는 false

			tcpModbusConnection.executeCoilWrite(address, value, unitId);

			// 각 쓰기 작업 후 값 확인
			SimpleDigitalOut digitalOut = (SimpleDigitalOut)processImage.getDigitalOut(address);
			assertEquals(value, digitalOut.isSet(), "Address " + address + "의 값이 예상과 다릅니다");
		}
	}

	@Test
	@DisplayName("연결 종료 후 재연결 테스트")
	void reconnect_연결_종료_후_재연결() throws IOException, ModbusException {
		// Given - 첫 번째 연결
		tcpModbusConnection.connect();
		assertTrue(tcpModbusConnection.isConnected());

		// 코일 쓰기 수행
		tcpModbusConnection.executeCoilWrite(0, true, 1);

		// When - 연결 종료 후 재연결
		tcpModbusConnection.close();
		assertFalse(tcpModbusConnection.isConnected());

		tcpModbusConnection.connect();

		// Then - 재연결 후 다시 동작 확인
		assertTrue(tcpModbusConnection.isConnected());
		tcpModbusConnection.executeCoilWrite(1, false, 1);

		SimpleDigitalOut digitalOut = (SimpleDigitalOut)processImage.getDigitalOut(1);
		assertFalse(digitalOut.isSet());
	}

	@Test
	@DisplayName("잘못된 포트로 연결 시도 시 실패 테스트")
	void connect_잘못된_포트_연결_실패() throws IOException {
		// Given - 사용하지 않는 포트 찾기
		int unavailablePort = findAvailablePort();
		TcpModbusConnection wrongPortConnection = new TcpModbusConnection(TEST_HOST, unavailablePort);

		// When & Then
		assertThrows(IOException.class, wrongPortConnection::connect);
	}

	/**
	 * 사용 가능한 포트를 찾아서 반환합니다.
	 *
	 * @return 사용 가능한 포트 번호
	 * @throws IOException 사용 가능한 포트를 찾을 수 없는 경우
	 */
	private int findAvailablePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();

			// 포트가 유효한 범위인지 확인 (1024-65535)
			if (port < 1024 || port > 65535) {
				throw new IOException("유효하지 않은 포트 범위: " + port);
			}

			return port;
		}
	}

	private void setupModbusServer() throws Exception {
		// 프로세스 이미지 생성 (코일과 레지스터 등을 관리)
		processImage = new SimpleProcessImage();

		// 테스트용 코일 생성 (주소 0-9)
		for (int i = 0; i < 10; i++) {
			processImage.addDigitalOut(new SimpleDigitalOut());
		}

		// Modbus TCP 슬레이브 생성 - 동적으로 할당된 포트 사용
		modbusServer = ModbusSlaveFactory.createTCPSlave(testPort, 3);
		modbusServer.addProcessImage(1, processImage); // Unit ID 1
		modbusServer.open();
	}
}
