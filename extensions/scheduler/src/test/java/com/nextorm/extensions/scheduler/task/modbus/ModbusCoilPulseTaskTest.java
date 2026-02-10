package com.nextorm.extensions.scheduler.task.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ModbusCoilPulseTask 테스트")
class ModbusCoilPulseTaskTest {

	private FakeModbusConnection fakeConnection;
	private FakeModbusConnectionFactory fakeConnectionFactory;
	private ModbusCoilPulseTask task;

	@BeforeEach
	void setUp() {
		fakeConnection = new FakeModbusConnection();
		fakeConnectionFactory = new FakeModbusConnectionFactory(fakeConnection);
	}

	@Test
	@DisplayName("단일 주소에 대해 코일 펄스가 성공적으로 실행됨")
	void singleAddressCoilPulseExecutesSuccessfully() {
		// Given
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		// When
		task.execute();

		// Then
		assertThat(fakeConnection.isConnected()).isFalse();
		assertThat(fakeConnection.isClosed()).isTrue();
		assertThat(fakeConnection.getCoilWrites()).hasSize(2);
		assertThat(fakeConnection.getCoilWrites()
								 .get(0)).isEqualTo(new CoilWriteRecord(100, true, 1));
		assertThat(fakeConnection.getCoilWrites()
								 .get(1)).isEqualTo(new CoilWriteRecord(100, false, 1));
	}

	@Test
	@DisplayName("여러 주소에 대해 코일 펄스가 순차적으로 실행됨")
	void multipleAddressesCoilPulseExecutesSequentially() {
		// Given
		Map<String, Object> properties = createProperties("192.168.1.100", 503, 2, "100,101,102");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		// When
		task.execute();

		// Then
		assertThat(fakeConnection.getCoilWrites()).hasSize(6);
		assertThat(fakeConnection.getCoilWrites()
								 .get(0)).isEqualTo(new CoilWriteRecord(100, true, 2));
		assertThat(fakeConnection.getCoilWrites()
								 .get(1)).isEqualTo(new CoilWriteRecord(100, false, 2));
		assertThat(fakeConnection.getCoilWrites()
								 .get(2)).isEqualTo(new CoilWriteRecord(101, true, 2));
		assertThat(fakeConnection.getCoilWrites()
								 .get(3)).isEqualTo(new CoilWriteRecord(101, false, 2));
		assertThat(fakeConnection.getCoilWrites()
								 .get(4)).isEqualTo(new CoilWriteRecord(102, true, 2));
		assertThat(fakeConnection.getCoilWrites()
								 .get(5)).isEqualTo(new CoilWriteRecord(102, false, 2));
	}

	@Test
	@DisplayName("연결 생성 중 IOException 발생시 RuntimeException으로 감싸서 던짐")
	void connectionCreationIOExceptionWrappedInRuntimeException() {
		// Given
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, (host, port) -> {
			throw new IOException();
		});

		// When & Then
		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class)
												.hasCause(new IOException());
	}

	@Test
	@DisplayName("코일 쓰기 중 ModbusException 발생시 RuntimeException으로 감싸서 던짐")
	void coilWriteModbusExceptionWrappedInRuntimeException() {
		// Given
		ModbusException coilWriteException = new ModbusException("Modbus 통신 오류");
		fakeConnection.setCoilWriteException(coilWriteException);
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		// When & Then
		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class)
												.hasCause(coilWriteException);
		assertThat(fakeConnection.isClosed()).isTrue();
	}

	@Test
	@DisplayName("연결 팩토리에서 예외 발생시 연결 객체가 null이므로 close 호출되지 않음")
	void connectionFactoryExceptionDoesNotCallClose() {
		// Given
		fakeConnectionFactory.setCreateConnectionException(new RuntimeException("연결 팩토리 오류"));
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		// When & Then
		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);
		assertThat(fakeConnection.isClosed()).isFalse();
	}

	@Test
	@DisplayName("코일 쓰기 중 예외 발생해도 연결은 정상적으로 닫힘")
	void connectionClosedEvenWhenCoilWriteFails() {
		// Given
		fakeConnection.setCoilWriteExceptionOnCall(1, new ModbusException("두 번째 코일 쓰기에서 오류"));
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100,101");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		// When & Then
		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);
		assertThat(fakeConnection.isClosed()).isTrue();
		assertThat(fakeConnection.getCoilWrites()).hasSize(1);
	}

	@Test
	@DisplayName("연결 중 IOException 발생시 RuntimeException으로 감싸서 던짐")
	void connectionIOExceptionWrappedInRuntimeException() {
		IOException connectionException = new IOException("연결 실패");
		fakeConnection.setConnectException(connectionException);
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class)
												.hasCause(connectionException);
		assertThat(fakeConnection.isClosed()).isTrue();
	}

	@Test
	@DisplayName("일반 예외 발생시 RuntimeException으로 감싸서 던짐")
	void generalExceptionWrappedInRuntimeException() {
		RuntimeException generalException = new RuntimeException("예상치 못한 오류");
		fakeConnection.setConnectException(generalException);
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class)
												.hasMessage("예상치 못한 오류");
		assertThat(fakeConnection.isClosed()).isTrue();
	}

	@Test
	@DisplayName("첫 번째 코일 쓰기에서 실패해도 연결은 정상적으로 닫힘")
	void connectionClosedEvenWhenFirstCoilWriteFails() {
		fakeConnection.setCoilWriteExceptionOnCall(0, new ModbusException("첫 번째 코일 쓰기에서 오류"));
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);
		assertThat(fakeConnection.isClosed()).isTrue();
		assertThat(fakeConnection.getCoilWrites()).isEmpty();
	}

	@Test
	@DisplayName("OFF 코일 쓰기에서 실패해도 연결은 정상적으로 닫힘")
	void connectionClosedEvenWhenOffCoilWriteFails() {
		fakeConnection.setCoilWriteExceptionOnCall(1, new ModbusException("OFF 코일 쓰기에서 오류"));
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);
		assertThat(fakeConnection.isClosed()).isTrue();
		assertThat(fakeConnection.getCoilWrites()).hasSize(1);
		assertThat(fakeConnection.getCoilWrites()
								 .get(0)).isEqualTo(new CoilWriteRecord(100, true, 1));
	}

	@Test
	@DisplayName("여러 주소 중 중간 주소에서 실패해도 연결은 정상적으로 닫힘")
	void connectionClosedEvenWhenMiddleAddressFails() {
		fakeConnection.setCoilWriteExceptionOnCall(2, new ModbusException("중간 주소에서 오류"));
		Map<String, Object> properties = createProperties("localhost", 502, 1, "100,101,102");
		ModbusCoilPulseProperties coilProperties = new ModbusCoilPulseProperties(properties);
		task = new ModbusCoilPulseTask(coilProperties, fakeConnectionFactory);

		assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);
		assertThat(fakeConnection.isClosed()).isTrue();
		assertThat(fakeConnection.getCoilWrites()).hasSize(2);
		assertThat(fakeConnection.getCoilWrites()
								 .get(0)).isEqualTo(new CoilWriteRecord(100, true, 1));
		assertThat(fakeConnection.getCoilWrites()
								 .get(1)).isEqualTo(new CoilWriteRecord(100, false, 1));
	}

	private Map<String, Object> createProperties(
		String host,
		int port,
		int unitId,
		String addresses
	) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("host", host);
		properties.put("port", port);
		properties.put("unitId", unitId);
		properties.put("addresses", addresses);
		return properties;
	}

	private static class FakeModbusConnection implements ModbusConnection {
		private boolean connected = false;
		private boolean closed = false;
		private final List<CoilWriteRecord> coilWrites = new ArrayList<>();
		private Exception connectException;
		private Exception coilWriteException;
		private Map<Integer, Exception> coilWriteExceptionOnCall = new HashMap<>();
		private int coilWriteCallCount = 0;

		@Override
		public void connect() throws IOException {
			if (connectException instanceof IOException) {
				throw (IOException)connectException;
			}
			if (connectException instanceof RuntimeException) {
				throw (RuntimeException)connectException;
			}
			connected = true;
		}

		@Override
		public void executeCoilWrite(
			int address,
			boolean value,
			int unitId
		) throws ModbusException {
			if (coilWriteExceptionOnCall.containsKey(coilWriteCallCount)) {
				Exception exception = coilWriteExceptionOnCall.get(coilWriteCallCount);
				if (exception instanceof ModbusException) {
					throw (ModbusException)exception;
				}
			}
			if (coilWriteException instanceof ModbusException) {
				throw (ModbusException)coilWriteException;
			}
			coilWrites.add(new CoilWriteRecord(address, value, unitId));
			coilWriteCallCount++;
		}

		@Override
		public void close() {
			connected = false;
			closed = true;
		}

		@Override
		public boolean isConnected() {
			return connected;
		}

		public boolean isClosed() {
			return closed;
		}

		public List<CoilWriteRecord> getCoilWrites() {
			return new ArrayList<>(coilWrites);
		}

		public void setCoilWriteException(Exception exception) {
			this.coilWriteException = exception;
		}

		public void setCoilWriteExceptionOnCall(
			int callNumber,
			Exception exception
		) {
			this.coilWriteExceptionOnCall.put(callNumber, exception);
		}

		public void setConnectException(Exception exception) {
			this.connectException = exception;
		}
	}

	private static class FakeModbusConnectionFactory implements ModbusConnectionFactory {
		private final ModbusConnection connection;
		private Exception createConnectionException;

		public FakeModbusConnectionFactory(ModbusConnection connection) {
			this.connection = connection;
		}

		@Override
		public ModbusConnection createConnection(
			String host,
			int port
		) {
			if (createConnectionException != null) {
				if (createConnectionException instanceof RuntimeException) {
					throw (RuntimeException)createConnectionException;
				}
			}
			return connection;
		}

		public void setCreateConnectionException(Exception exception) {
			this.createConnectionException = exception;
		}
	}

	private static record CoilWriteRecord(int address, boolean value, int unitId) {
	}
}