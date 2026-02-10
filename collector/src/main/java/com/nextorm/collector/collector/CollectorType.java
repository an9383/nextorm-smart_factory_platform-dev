package com.nextorm.collector.collector;

import com.nextorm.collector.collector.hookcollector.WebHookCollector;
import com.nextorm.collector.collector.opcua.OpcUaPollingCollector;
import com.nextorm.collector.collector.opcuacollector.OpcUaCollector;
import com.nextorm.common.define.collector.ArgumentType;
import com.nextorm.common.define.collector.CollectorArgument;
import lombok.Getter;

import java.util.List;

@Getter
public enum CollectorType {
	HTTP_EMULATOR("에뮬레이터",
		List.of(CollectorArgument.of("apiUrl", ArgumentType.STRING)),
		HttpEmulatorCollector.class), HTTP_ECOPEACE("에코피스",
		List.of(CollectorArgument.of("apiUrl", ArgumentType.STRING)),
		HttpEcopeaceCollector.class), HTTP_ECOPEACE_PHOTO("에코피스 사진",
		List.of(CollectorArgument.of("photoApiUrl", ArgumentType.STRING),
			CollectorArgument.of("geoApiUrl", ArgumentType.STRING)),
		HttpEcopeacePhotoCollector.class), WEBHOOK_CROSSWALK("횡단보도 웹훅",
		List.of(CollectorArgument.of("port", ArgumentType.STRING)),
		WebHookCollector.class), MONGDB_SHINHEUNG("신흥정공", List.of(), MongoDbShinheungCollector.class), MODBUS_TCP(
		"MODBUS_TCP",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("registerType", ArgumentType.STRING),
			CollectorArgument.of("address", ArgumentType.STRING),
			CollectorArgument.of("dataType", ArgumentType.STRING),
			CollectorArgument.of("isZeroBase", ArgumentType.STRING),
			CollectorArgument.of("parameterList", ArgumentType.STRING)),
		ModbusTCPCollector.class), SOCKET_SCALE("저울 소켓통신",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("reconnectionInterval", ArgumentType.STRING),
			CollectorArgument.of("resetValue", ArgumentType.STRING)),
		ScaleSocketCollector.class), VIRTUAL_CSV("가상 CSV 데이터",
		List.of(CollectorArgument.of("isRepeat", ArgumentType.STRING),
			CollectorArgument.of("isPastData", ArgumentType.STRING),
			CollectorArgument.of("csvFilePath", ArgumentType.STRING)),
		VirtualCsvCollector.class), MODBUS_TCP_V2("MODBUS_TCP_V2",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("isZeroBase", ArgumentType.STRING),
			CollectorArgument.of("Mapping",
				ArgumentType.PARAMETER_MAPPING,
				List.of(new CollectorArgument.ExtraDataDefine("registerType",
						CollectorArgument.ExtraDataDefine.DataType.STRING),
					new CollectorArgument.ExtraDataDefine("address", CollectorArgument.ExtraDataDefine.DataType.STRING),
					new CollectorArgument.ExtraDataDefine("dataType",
						CollectorArgument.ExtraDataDefine.DataType.STRING),
					new CollectorArgument.ExtraDataDefine("scaleData",
						CollectorArgument.ExtraDataDefine.DataType.NUMBER),
					new CollectorArgument.ExtraDataDefine("extraAddress",
						CollectorArgument.ExtraDataDefine.DataType.STRING,
						false)))),
		ModbusTCPCollectorV2.class), ROBOSHOT_MONITOR("ROBOSHOT_MONITOR",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("machineId", ArgumentType.STRING),
			CollectorArgument.of("exePath", ArgumentType.STRING),
			CollectorArgument.of("Mapping",
				ArgumentType.PARAMETER_MAPPING,
				List.of(new CollectorArgument.ExtraDataDefine("dataIndex",
					CollectorArgument.ExtraDataDefine.DataType.NUMBER)))),
		RoboshotMonitorCollector.class), OPCUA("OPCUA",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("xmlFileName", ArgumentType.STRING),
			CollectorArgument.of("isSubscription", ArgumentType.STRING)),
		OpcUaCollector.class), MODBUS_TCP_LS("MODBUS_TCP_LS",
		List.of(CollectorArgument.of("ip", ArgumentType.STRING),
			CollectorArgument.of("port", ArgumentType.STRING),
			CollectorArgument.of("isZeroBase", ArgumentType.STRING),
			CollectorArgument.of("Mapping",
				ArgumentType.PARAMETER_MAPPING,
				List.of(new CollectorArgument.ExtraDataDefine("address",
						CollectorArgument.ExtraDataDefine.DataType.STRING),
					new CollectorArgument.ExtraDataDefine("dataType",
						CollectorArgument.ExtraDataDefine.DataType.STRING)))),
		ModbusTCPLSCollector.class), OPCUA_POLLING("OPCUA_POLLING",
		List.of(CollectorArgument.of("endpointUrl", ArgumentType.STRING),
			CollectorArgument.of("Mapping",
				ArgumentType.PARAMETER_MAPPING,
				List.of(new CollectorArgument.ExtraDataDefine("nodeId",
					CollectorArgument.ExtraDataDefine.DataType.STRING,
					true)))),
		OpcUaPollingCollector.class), REST_API_COLLECTOR("RestApi Collector",
		List.of(CollectorArgument.of("url", ArgumentType.STRING),
			CollectorArgument.of("method", ArgumentType.STRING),
			CollectorArgument.of("Mapping",
				ArgumentType.PARAMETER_MAPPING,
				List.of(new CollectorArgument.ExtraDataDefine("jsonPath",
					CollectorArgument.ExtraDataDefine.DataType.STRING,
					true)))),
		RestApiCollector.class);

	/*
	 * CollectorType의 정의 버전
	 * 변경사항이 있을 경우, 버전을 올려야 한다
	 * 형식: YYYYMMDDXX
	 * YYYY: 연도, MM: 월, DD: 일, XX: 변경 횟수
	 * 예시: 2025072101은 2025년 7월 21일에 정의된 첫 번째 버전이다
	 * 이 버전은 CollectorType의 정의가 변경되었음을 나타낸다
	 */
	public static final int DEFINITION_VERSION = 2025081001;

	private final String displayName;    // UI에 표기하기 위한 이름
	private final List<CollectorArgument> arguments;    // 컬렉터에서 필수로 받아야 하는 인자 정보
	private final Class<? extends Collector> collectorClass;    // 컬렉터 구현 클래스

	CollectorType(
		String displayName,
		List<CollectorArgument> arguments,
		Class<? extends Collector> collectorClass
	) {
		this.displayName = displayName;
		this.arguments = arguments;
		this.collectorClass = collectorClass;
	}
}
