package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusTCPLSCollector implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;
	private final ObjectMapper objectMapper;
	private final List<DataCollectPlan.Parameter> collectParameters;
	private TCPMasterConnection connection = null;
	private ModbusTCPTransaction transaction = null;
	private final String ip;
	private final int port;
	private final boolean isZeroBase;

	/**
	 * Modbus TCP 컬렉터 초기화
	 *
	 * @param config       데이터 수집 설정 정보
	 * @param sender       데이터를 전송하는 객체
	 * @param objectMapper JSON 변환 객체
	 */
	public ModbusTCPLSCollector(
		DataCollectPlan config,
		Sender sender,
		ObjectMapper objectMapper
	) {
		this.config = config;
		this.sender = sender;
		this.objectMapper = objectMapper;

		ip = config.getCollectorArguments()
				   .get("ip")
				   .toString();
		port = Integer.parseInt(config.getCollectorArguments()
									  .get("port")
									  .toString());
		isZeroBase = Boolean.parseBoolean(config.getCollectorArguments()
												.get("isZeroBase")
												.toString());

		collectParameters = config.getCollectParameters();
		modbusTCPConnection(ip, port);
	}

	/**
	 * Modbus TCP 서버와 연결을 설정하는 메서드
	 *
	 * @param ip   연결할 Modbus 장치의 IP 주소
	 * @param port Modbus 장치의 포트 번호
	 */
	private void modbusTCPConnection(
		String ip,
		int port
	) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			connection = new TCPMasterConnection(addr);
			connection.setPort(port);
			connection.connect();
			transaction = new ModbusTCPTransaction(connection);
			//log.info("Modbus TCP 연결 성공: {}:{}", ip, port);
		} catch (Exception e) {
			log.error("Modbus TCP 연결 실패", e);
		}
	}

	/**
	 * 데이터 수집 루프 실행
	 */
	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {
			try {
				Map<String, Object> data = getData();
				if (!data.isEmpty()) {
					SendMessage sendMessage = paramValuesToMessage(data);
					sender.send(config.getTopic(), sendMessage);
				}
			} catch (RuntimeException e) {
				log.error("컬렉터 종료 가능 에러 발생. DCP_ID: {}", config.getDcpId(), e);
			}
			sleep(config.getDataInterval());
		}
	}

	/**
	 * 일정 시간 대기 (수집 주기에 따라 실행됨)
	 *
	 * @param intervalSeconds 대기 시간 (초 단위)
	 */
	private void sleep(int intervalSeconds) {
		try {
			Thread.sleep(intervalSeconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread()
				  .interrupt();
		}
	}

	/**
	 * Modbus 장치에서 데이터를 읽어오는 메서드
	 *
	 * @return 수집된 데이터 (파라미터명 → 값)
	 */
	private Map<String, Object> getData() {
		Map<String, Object> data = new HashMap<>();

		if (!connection.isConnected()) {
			log.error("Modbus 연결이 활성화되지 않았습니다. 다시 연결 시도 중...");
			modbusTCPConnection(ip, port);
			return data;
		}

		try {
			for (DataCollectPlan.Parameter detail : collectParameters) {
				MappingData mappingData = objectMapper.convertValue(detail.getExtraData(), MappingData.class);
				String parameter = detail.getName();
				String dataType = mappingData.dataType()
											 .toUpperCase();
				int address = getAddressForZeroBase(mappingData.address());
				int registerCount = getRegisterCountForDataType(dataType);

				Object value = readDataByType(address, registerCount, dataType, parameter);
				data.put(parameter, value);
			}
			return data;
		} catch (Exception e) {
			log.error("Modbus 데이터 읽기 오류: {}", e.getMessage());
			return data;
		}
	}

	/**
	 * 데이터 타입에 맞게 값을 변환
	 *
	 * @param address       데이터 주소
	 * @param registerCount 읽어야 할 레지스터 개수
	 * @param dataType      데이터 타입
	 * @param parameter     파라미터명
	 * @return 변환된 데이터 값 (double 타입으로 반환) 또는 null (범위 초과 시)
	 */
	private Object readDataByType(
		int address,
		int registerCount,
		String dataType,
		String parameter
	) {
		ByteBuffer buffer = readRegistersReversed(address, registerCount);
		if (buffer == null) {
			return null;
		}

		int requiredBytes = registerCount * 2;
		if (buffer.remaining() < requiredBytes) {
			log.warn("readDataByType - 버퍼 데이터 부족. address={}, dataType={}, 필요 바이트={}, 실제 바이트={}",
				address,
				dataType,
				requiredBytes,
				buffer.remaining());
			return null;
		}

		return switch (dataType) {
			case "REAL" -> convertToDecimal(buffer.getFloat(), parameter, address, dataType); // 4바이트 부동소수점 (Float)
			case "INT" ->
				convertToDecimal(buffer.getShort(), parameter, address, dataType);   // 2바이트 (16비트) 정수 (Signed Short)
			case "UINT" -> buffer.getShort() & 0xFFFF;               // 2바이트 부호 없는 정수 (Unsigned Short)
			case "DINT" ->
				convertToDecimal(buffer.getInt(), parameter, address, dataType);    // 4바이트 (32비트) 정수 (Signed Int)
			case "UDINT" -> buffer.getInt() & 0xFFFFFFFFL;           // 4바이트 부호 없는 정수 (Unsigned Int)
			case "LINT", "ULINT" ->
				convertToDecimal(buffer.getLong(), parameter, address, dataType); // 8바이트 정수 (Signed/Unsigned Long)
			case "TIME", "DATE_AND_TIME" ->
				convertMicrosecondsToSeconds(buffer.getLong(), parameter, address, dataType); // 8바이트 시간 변환
			case "BOOL" -> buffer.getShort() != 0
						   ? 1.0
						   : 0.0;       // 2바이트 불리언을 double(1.0/0.0)으로 변환
			case "ALARM", "WORD", "ALARMS_PEND_ALL" -> buffer.getShort() & 0xFFFF; // 2바이트 부호 없는 정수
			default -> throw new IllegalArgumentException("readDataByType - 지원되지 않는 데이터 유형: " + dataType);
		};
	}

	/**
	 * 데이터 타입별 레지스터 개수 반환
	 *
	 * @param dataType 데이터 타입
	 * @return 해당 데이터 타입이 필요한 레지스터 개수
	 */
	private int getRegisterCountForDataType(String dataType) {
		return switch (dataType) {
			case "REAL", "DINT", "UDINT" -> 2;
			case "LINT", "ULINT", "TIME", "DATE_AND_TIME" -> 4;
			case "INT", "UINT", "BOOL", "ALARM", "WORD", "ALARMS_PEND_ALL" -> 1;
			default -> throw new IllegalArgumentException("getRegisterCountForDataType - 알 수 없는 데이터 타입: " + dataType);
		};
	}

	/**
	 * Modbus 장치에서 값을 읽어와 역순으로 정렬하는 메서드
	 *
	 * @param address 데이터 주소
	 * @param count   읽어야 할 레지스터 개수
	 * @return 읽어온 데이터 (ByteBuffer)
	 */
	private ByteBuffer readRegistersReversed(
		int address,
		int count
	) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, count);
			request.setUnitID(1);
			transaction.setRequest(request);
			transaction.execute();
			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();

			// response == null 방지
			if (response == null) {
				log.warn("readRegistersReversed - Modbus 응답이 NULL입니다. address={}, count={}", address, count);
				return null;
			}

			// response.getRegisterCount() < count 방지
			if (response.getWordCount() < count) {
				log.warn("readRegistersReversed - 응답된 레지스터 개수가 부족합니다! address={}, 요청 개수={}, 응답 개수={}",
					address,
					count,
					response.getWordCount());
				return null;
			}

			ByteBuffer buffer = ByteBuffer.allocate(count * 2);

			// **레지스터 값을 역순으로 읽기**
			for (int i = count - 1; i >= 0; i--) {
				buffer.putShort((short)response.getRegister(i)
											   .getValue());
			}
			buffer.rewind();
			return buffer;
		} catch (Exception e) {
			log.error("readRegistersReversed - 레지스터 읽기 오류 (주소: {}, 개수: {})", address, count, e);
			return null;
		}
	}

	/**
	 * 부호 있는 값(음수 포함)을 DECIMAL(15,5) 형식에 맞게 변환
	 *
	 * @param value     변환할 숫자 (float, int, long 등)
	 * @param parameter 파라미터명
	 * @param address   Modbus 주소
	 * @param dataType  데이터 타입
	 * @return 소수점 5자리까지만 유지된 값 또는 null (범위 초과 시)
	 */
	private Double convertToDecimal(
		Number value,
		String parameter,
		int address,
		String dataType
	) {
		double doubleValue = value.doubleValue();

		// 정수 여부 확인 (소수부가 0이면 정수 그대로 반환)
		if (doubleValue == Math.floor(doubleValue)) {
			return doubleValue;
		}

		// 소수점 5자리까지만 유지 (소수점이 있는 경우)
		return formatDecimal(doubleValue, parameter, address, dataType);
	}

	/**
	 * 소수점 5자리까지만 유지하고 정수부가 10자리 초과되지 않도록 변환
	 *
	 * @param value     변환할 숫자
	 * @param parameter 파라미터명
	 * @param address   Modbus 주소
	 * @param dataType  데이터 타입
	 * @return 변환된 값 (DECIMAL(15,5) 형식) 또는 null (범위 초과 시)
	 */
	private Double formatDecimal(
		double value,
		String parameter,
		int address,
		String dataType
	) {
		BigDecimal formattedValue = BigDecimal.valueOf(value)
											  .setScale(5, RoundingMode.HALF_UP);

		if (formattedValue.precision() - formattedValue.scale() > 10) {
			log.warn(
				"formatDecimal - 변환된 값이 DECIMAL(15,5) 범위를 초과함. parameter={}, address={}, dataType={}, value={}, formattedValue={}",
				parameter,
				address,
				dataType,
				value,
				formattedValue);
			return null;
		}

		return formattedValue.doubleValue();
	}

	/**
	 * 마이크로초(μs) 값을 초(s) 단위로 변환하여 DECIMAL(15,5) 형식에 맞게 조정
	 *
	 * @param microseconds 마이크로초 단위의 시간 값
	 * @return 변환된 초(s) 값 (소수점 5자리, 정수부 10자리 이하)
	 */
	private double convertMicrosecondsToSeconds(
		long microseconds,
		String parameter,
		int address,
		String dataType
	) {
		// 1. 초 단위 변환 (소수점 포함)
		double seconds = microseconds / 1_000_000.0;

		// 2. BigDecimal을 이용하여 소수점 5자리까지만 유지 (반올림)
		BigDecimal formattedSeconds = BigDecimal.valueOf(seconds)
												.setScale(5, RoundingMode.HALF_UP);

		// 3. 정수부가 10자리 초과하면 10자리까지만 유지
		if (formattedSeconds.precision() - formattedSeconds.scale() > 10) {
			log.warn(
				"convertMicrosecondsToSeconds - 변환된 값이 DECIMAL(15,5) 범위를 초과함. parameter={}, address={}, dataType={}, microseconds={}, formattedSeconds={}",
				parameter,
				address,
				dataType,
				microseconds,
				formattedSeconds);
			throw new ArithmeticException("변환된 값이 DECIMAL(15,5) 범위를 초과합니다: " + formattedSeconds);
		}

		return formattedSeconds.doubleValue();
	}

	/**
	 * 수집된 데이터 값을 메시지로 변환하여 반환
	 *
	 * @param paramsValueMap 수집된 데이터 맵 (파라미터명 → 값)
	 * @return SendMessage 변환된 메시지 객체
	 * @throws IllegalArgumentException 수집된 파라미터 개수가 설정된 개수와 일치하지 않을 경우 예외 발생
	 */
	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();

		// 수집된 파라미터 개수가 설정된 개수와 일치하는지 검증
		if (paramsValueMap.size() != collectParameterSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
				collectParameterSize,
				paramsValueMap.size());
			throw new IllegalArgumentException(message);
		}

		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	/**
	 * Modbus 주소를 0 기반 주소로 변환
	 * <p>
	 * Modbus 프로토콜은 장치마다 0 기반 또는 1 기반 주소 체계를 사용할 수 있으므로,
	 * 이를 설정값(`isZeroBase`)에 따라 변환하여 사용함.
	 *
	 * @param address 원본 주소 값
	 * @return 변환된 주소 값 (0 기반 주소)
	 */
	private int getAddressForZeroBase(Integer address) {
		return isZeroBase
			   ? address - 1
			   : address;
	}

	/**
	 * 현재 설정된 데이터 수집 계획을 반환
	 *
	 * @return DataCollectPlan 현재 사용 중인 데이터 수집 계획
	 */
	@Override
	public DataCollectPlan getConfig() {
		return config;
	}

	/**
	 * 데이터 매핑 정보를 저장하는 레코드 클래스
	 * <p>
	 * Modbus에서 데이터를 수집할 때 필요한 정보를 담고 있으며,
	 * 특정 주소의 데이터가 어떤 형식으로 변환되어야 하는지에 대한 정보를 포함한다.
	 *
	 * @param address  데이터가 저장된 Modbus 십진수 주소
	 * @param dataType 데이터 타입 (예: "INT", "REAL", "TIME" 등)
	 */
	record MappingData(int address, String dataType) {
	}
}
