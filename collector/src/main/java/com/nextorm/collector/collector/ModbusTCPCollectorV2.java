package com.nextorm.collector.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ModbusTCPCollectorV2 implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;
	private final ObjectMapper objectMapper;

	private final List<DataCollectPlan.Parameter> collectParameters;

	private TCPMasterConnection connection = null;
	ModbusTCPTransaction transaction = null;
	private final String ip;
	private final String port;
	private final boolean isZeroBase;    // true 면 0기반 요청(address 에서 -1), false 이면 1기반 요청 address 그대로 요청

	public ModbusTCPCollectorV2(
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
		port = config.getCollectorArguments()
					 .get("port")
					 .toString();
		isZeroBase = Boolean.parseBoolean(config.getCollectorArguments()
												.get("isZeroBase")
												.toString());

		collectParameters = config.getCollectParameters();
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
			transaction = new ModbusTCPTransaction(connection);
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

			sleep(config.getDataInterval());
		}
	}

	private void sleep(int intervalSeconds) {
		try {
			Thread.sleep(intervalSeconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread()
				  .interrupt();
		}
	}

	private Map<String, Object> getData() {
		Map<String, Object> data = new HashMap<>();

		if (!connection.isConnected()) {
			log.error("Modbus 연결이 활성화되지 않았습니다.");
			modbusTCPConnection(ip, port);
			return data;
		}

		try {
			for (DataCollectPlan.Parameter detail : collectParameters) {
				MappingData mappingData = objectMapper.convertValue(detail.getExtraData(), MappingData.class);
				String parameter = detail.getName();
				int extraAddress = 0;
				int address = getAddressForZeroBase(mappingData.address());

				if (mappingData.extraAddress() != null) {
					extraAddress = getAddressForZeroBase(mappingData.extraAddress()
																	.intValue());
				}

				switch (mappingData.registerType()) {
					case "holding" -> data.put(parameter,
						processHoldingRegister(address, mappingData.dataType(), (double)mappingData.scaleData()));
					case "input" -> data.put(parameter,
						processInputRegister(address, mappingData.dataType(), (double)mappingData.scaleData()));
					case "discrete" -> data.put(parameter, readBitData(address));
					case "extra" -> data.put(parameter,
						processExtraAddressData(address,
							mappingData.dataType(),
							mappingData.scaleData(),
							extraAddress));
					default -> log.warn("지원되지 않는 레지스터 유형: {}", mappingData.registerType());
				}
			}
			return data;
		} catch (Exception e) {
			log.error("Modbus 데이터 읽기 중 오류 발생, e");
			return data;
		}

	}

	private int getAddressForZeroBase(Integer address) {
		return isZeroBase
			   ? address - 1
			   : address;
	}

	private Object processExtraAddressData(
		int address,
		String dataType,
		double scaleData,
		int extraAddress
	) {
		return switch (dataType) {
			case "REAL" -> readFloatByTwoAdresses(address, extraAddress, scaleData);
			case "WORD" -> readAndDecodeRegister(address, extraAddress, scaleData);
			default -> throw new IllegalArgumentException("지원되지 않는 데이터 유형: " + dataType);
		};
	}

	/*
		address값과 extraAddress값 2개로 결합 다중 레지스터 값 조합하여 값을 가져온다.
	 */
	private Object readFloatByTwoAdresses(
		int address,
		int extraAddress,
		double scaleData
	) {
		try {
			ReadMultipleRegistersRequest lowRequest = new ReadMultipleRegistersRequest(address, 1);
			transaction.setRequest(lowRequest);
			transaction.execute();
			int low = ((ReadMultipleRegistersResponse)transaction.getResponse()).getRegisterValue(0);

			ReadMultipleRegistersRequest highRequest = new ReadMultipleRegistersRequest(extraAddress, 1);
			transaction.setRequest(highRequest);
			transaction.execute();
			int high = ((ReadMultipleRegistersResponse)transaction.getResponse()).getRegisterValue(0);

			int combined = (high << 16) | (low & 0xFFFF);

			return (combined / scaleData);
		} catch (Exception e) {
			log.error("REAL 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	/**
	 * HoldingRester값을 읽어서 두개의 주소값을 AND처리 한다.
	 */
	private Object readAndDecodeRegister(
		int address,
		int extraAddress,
		Double scaleData
	) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 1);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();
			int registerValue = response.getRegisterValue(0);
			int registerAndExtra = registerValue & extraAddress;
			Double result = registerAndExtra / scaleData;

			return (result / scaleData);
		} catch (Exception e) {
			log.error("WORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Object processHoldingRegister(
		int address,
		String dataType,
		Double scaleData
	) {
		return switch (dataType) {
			case "WORD" -> readHoldingRegister(address, scaleData);
			case "REAL" -> readFloat(address, scaleData);
			case "DWORD" -> readDwordHoldingRegister(address, scaleData);
			default -> throw new IllegalArgumentException("지원되지 않는 데이터 유형: " + dataType);
		};
	}

	private Object processInputRegister(
		int address,
		String dataType,
		Double scaleData
	) {
		return switch (dataType) {
			case "WORD" -> readInputRegister(address, scaleData);
			default -> throw new IllegalArgumentException("지원되지 않는 데이터 유형: " + dataType);
		};
	}

	private Double readInputRegister(
		int address,
		Double scaleData
	) {
		try {
			ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, 1);
			request.setUnitID(1);
			transaction.setRequest(request);
			transaction.execute();

			ReadInputRegistersResponse response = (ReadInputRegistersResponse)transaction.getResponse();
			return response.getRegisterValue(0) / scaleData;
		} catch (Exception e) {
			log.error("WORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readHoldingRegister(
		int address,
		Double scaleData
	) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 1);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();

			return response.getRegisterValue(0) / scaleData;
		} catch (Exception e) {
			log.error("WORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readFloat(
		int address,
		Double scaleData
	) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 2);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();
			int high = response.getRegister(0)
							   .getValue();
			int low = response.getRegister(1)
							  .getValue();

			int combined = (high << 16) | (low & 0xFFFF);

			return (combined / scaleData);
		} catch (Exception e) {
			log.error("REAL 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Double readDwordHoldingRegister(
		int address,
		Double scaleData
	) {
		try {
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(address, 2);
			transaction.setRequest(request);
			transaction.execute();

			ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse)transaction.getResponse();

			Register[] registers = response.getRegisters();
			int highWord = registers[0].getValue() & 0xFFFF;
			int lowWord = registers[1].getValue() & 0xFFFF;
			int intBits = (highWord << 16) | lowWord;

			float floatValue = Float.intBitsToFloat(intBits);
			return floatValue / scaleData;

		} catch (Exception e) {
			log.error("DWORD 데이터 읽기 오류 (주소: {})", address, e);
			return null;
		}
	}

	private Integer readBitData(int address) {
		try {
			ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(address, 1);
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

	record MappingData(String registerType, int address, String dataType, int scaleData, Integer extraAddress) {
	}
}
