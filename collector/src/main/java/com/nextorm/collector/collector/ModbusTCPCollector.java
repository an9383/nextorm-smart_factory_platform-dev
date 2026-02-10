package com.nextorm.collector.collector;

import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusTCPCollector implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;
	private TCPMasterConnection connection = null;
	private final String ip;
	private final String port;
	private final String registerType;    // 레지스터 유형: holding, input, discrete(float 32bit)
	private final String address;
	private final String dataType;    // 데이터 유형: WORD, REAL, BIT
	private final String parameterList;
	private final boolean isZeroBase;    // true 면 0기반 요청(address 에서 -1), false 이면 1기반 요청 address 그대로 요청

	private final List<String> collectParameterNames;

	public ModbusTCPCollector(
		DataCollectPlan config,
		Sender sender
	) {
		this.config = config;
		this.sender = sender;
		ip = config.getCollectorArguments()
				   .get("ip")
				   .toString();
		port = config.getCollectorArguments()
					 .get("port")
					 .toString();
		registerType = config.getCollectorArguments()
							 .get("registerType")
							 .toString();
		address = config.getCollectorArguments()
						.get("address")
						.toString();
		dataType = config.getCollectorArguments()
						 .get("dataType")
						 .toString();
		parameterList = config.getCollectorArguments()
							  .get("parameterList")
							  .toString();
		isZeroBase = Boolean.parseBoolean(config.getCollectorArguments()
												.get("isZeroBase")
												.toString());

		collectParameterNames = config.getCollectParameters()
									  .stream()
									  .map(DataCollectPlan.Parameter::getName)
									  .toList();

		modbusTCPConnection(ip, port);
	}

	private void modbusTCPConnection(
		String ip,
		String port
	) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			connection = new TCPMasterConnection(addr);
			connection.setPort(Integer.parseInt(port));
			connection.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
				log.error("컬렉터 종료 가능 에러가 발생. DCP_ID: {}", config.getDcpId(), e);
			}

			try {
				Thread.sleep(config.getDataInterval() * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread()
					  .interrupt();
			}

		}
	}

	private Map<String, Object> getData() {
		Map<String, Object> data = new HashMap<>();

		if (connection.isConnected()) {
			try {
				String[] registerTypes = registerType.split(",");
				String[] addresses = address.split(",");
				String[] dataTypes = dataType.split(",");
				String[] parameterArray = parameterList.split(",");

				for (int i = 0; i < registerTypes.length; i++) {
					String paramRegisterType = registerTypes[i].trim()
															   .toLowerCase();
					int addr = Integer.parseInt(addresses[i].trim());

					if (isZeroBase) {
						addr = addr - 1;
					}

					String paramDataType = dataTypes[i].trim()
													   .toUpperCase();
					String parameter = parameterArray[i].trim();

					switch (paramRegisterType) {
						case "holding" -> data.put(parameter, processHoldingRegister(addr, paramDataType));
						case "input" -> data.put(parameter, processInputRegister(addr, paramDataType));
						case "discrete" -> data.put(parameter, readBitData(addr));
						default -> log.warn("지원되지 않는 레지스터 유형: {}", registerType);
					}
				}
			} catch (Exception e) {
				log.error("Modbus 데이터 읽기 중 오류 발생, e");
				return data;
			}
		} else {
			log.error("Modbus 연결이 활성화되지 않았습니다.");
			modbusTCPConnection(ip, port);
			return data;
		}

		for (String name : collectParameterNames) {
			Object value = data.get(name);
			data.put(name, value);
		}

		return data;
	}

	private Object processHoldingRegister(
		int address,
		String dataType
	) {
		return switch (dataType) {
			case "WORD" -> readHoldingRegister(address);
			case "REAL" -> readFloat(address);
			case "SWAP_REAL" -> readFloatBySwap(address);
			case "DWORD" -> readDwordHoldingRegister(address);
			default -> throw new IllegalArgumentException("지원되지 않는 데이터 유형: " + dataType);
		};
	}

	private Object processInputRegister(
		int address,
		String dataType
	) {
		return switch (dataType) {
			case "WORD" -> readInputRegister(address);
			default -> throw new IllegalArgumentException("지원되지 않는 데이터 유형: " + dataType);
		};
	}

	private Double readInputRegister(int address) {
		try {
			ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, 1);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadInputRegistersResponse response = (ReadInputRegistersResponse)transaction.getResponse();
			return (Double)(double)response.getRegisterValue(0);
		} catch (Exception e) {
			log.error("WORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	/**
	 * TODO:: 이 메소드는 작성일 (2025.06.18) 기준으로, 사용된 적이 없음
	 * 태우수산 설비 작업이 완료되면, DWORD 레지스터를 읽을 필요가 있어 미리 구현을 하여 배포한 상태
	 * 실제 수집 시점에 문제가 있다면 수정, 문제가 없다면 이 TODO를 지워주세요
	 */
	private Double readDwordHoldingRegister(int address) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 2);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();

			Register[] registers = response.getRegisters();
			int highWord = registers[0].getValue() & 0xFFFF;
			int lowWord = registers[1].getValue() & 0xFFFF;
			int intBits = (highWord << 16) | lowWord;

			float floatValue = Float.intBitsToFloat(intBits);
			return (double)floatValue;

		} catch (Exception e) {
			log.error("DWORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readHoldingRegister(int address) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 1);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();
			return (Double)(double)response.getRegisterValue(0);
		} catch (Exception e) {
			log.error("WORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readFloat(int address) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 2);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();
			int high = response.getRegister(0)
							   .getValue();
			int low = response.getRegister(1)
							  .getValue();

			int combined = (high << 16) | (low & 0xFFFF);
			return (Double)(double)Float.intBitsToFloat(combined);
		} catch (Exception e) {
			log.error("REAL 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readFloatBySwap(int address) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 2);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();
			int high = response.getRegister(1)
							   .getValue();
			int low = response.getRegister(0)
							  .getValue();

			int combined = (high << 16) | (low & 0xFFFF);
			return (Double)(double)Float.intBitsToFloat(combined);
		} catch (Exception e) {
			log.error("REAL 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Integer readBitData(int address) {
		try {
			ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(address, 1);
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			transaction.setRequest(request);
			transaction.execute();

			ReadInputDiscretesResponse response = (ReadInputDiscretesResponse)transaction.getResponse();
			return response.getDiscreteStatus(0)
				   ? 1
				   : 0;
		} catch (Exception e) {
			log.error("BIT 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();

		if (paramsValueMap.size() != collectParameterSize) {
			String message = "수집대상 파라미터와 수집된 파라미터의 크기가 일치하지 않습니다. (parameter: %s, paramValue: %s)".formatted(
				collectParameterSize,
				paramsValueMap.size());
			throw new IllegalArgumentException(message);
		}

		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}
}
