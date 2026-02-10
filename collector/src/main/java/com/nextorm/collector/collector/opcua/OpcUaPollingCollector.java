package com.nextorm.collector.collector.opcua;

import com.nextorm.collector.collector.Collector;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpcUaPollingCollector implements Collector {
	private final DataCollectPlan config;
	private final Sender sender;

	private final String endpointUrl;
	private final List<TargetNode> targetNodes;

	private OpcUaClientWrapper opcClient;

	public OpcUaPollingCollector(
		DataCollectPlan collectPlan,
		Sender sender
	) {
		this.config = collectPlan;
		this.sender = sender;

		this.endpointUrl = collectPlan.getCollectorArguments()
									  .get("endpointUrl")
									  .toString();

		this.targetNodes = collectPlan.getCollectParameters()
									  .stream()
									  .map(this::toTargetNode)
									  .toList();
	}

	private TargetNode toTargetNode(DataCollectPlan.Parameter parameter) {
		String nodeId = parameter.getExtraData()
								 .get("nodeId")
								 .toString();
		return new TargetNode(nodeId, parameter.getName(), parameter.getId());
	}

	@Override
	public void run() {
		while (!Thread.currentThread()
					  .isInterrupted()) {
			try {
				if (opcClient == null) {
					opcClient = new OpcUaClientWrapper(endpointUrl);
				}

				boolean isConnected = opcClient.isConnected();
				if (!isConnected) {
					isConnected = opcClient.connect();
				}

				// OPC 클라이언트에 연결된것이 확인되지 않았다면, 수집을 건너뜀
				if (!isConnected) {
					log.info("OPC 클라이언트에 연결되지 않아 수집을 건너뜁니다");
				} else {

					OpcUaClientWrapper.NodesReadResult nodesReadResult = opcClient.readNodesData(getNodeIds());
					Map<String, Object> data = new HashMap<>();

					if (!nodesReadResult.isSuccess()) {
						log.error("OPC 클라이언트에서 노드 데이터를 읽는 데 실패했습니다. DCP_ID: {}", config.getDcpId());
					} else {
						List<OpcUaClientWrapper.NodeResult> nodes = nodesReadResult.getNodes();
						for (int i = 0; i < targetNodes.size(); i++) {
							TargetNode targetNode = targetNodes.get(i);
							OpcUaClientWrapper.NodeResult nodeResult = nodes.get(i);
							Object value = null;
							if (nodeResult.isSuccess()) {
								value = convertDataValue(nodeResult);
							}
							data.put(targetNode.parameterName, value);
						}
					}
					if (!data.isEmpty()) {
						SendMessage sendMessage = paramValuesToMessage(data);
						sender.send(config.getTopic(), sendMessage);
					}
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

	private List<String> getNodeIds() {
		return targetNodes.stream()
						  .map(TargetNode::nodeId)
						  .toList();
	}

	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public DataCollectPlan getConfig() {
		return this.config;
	}

	/**
	 * OPC UA 노드 데이터를 적절한 형태로 변환하는 함수
	 * boolean 타입은 integer로 변환 (true: 1, false: 0)
	 * 일반적으로 처리가능한 타입(Boolean, String, 숫자형) 외에는 UnsupportedOperationException 발생
	 *
	 * @param nodeResult OPC UA 노드 읽기 결과
	 * @return 변환된 데이터 값
	 * @throws UnsupportedOperationException 지원하지 않는 데이터 타입인 경우
	 */
	private Object convertDataValue(OpcUaClientWrapper.NodeResult nodeResult) {
		Object originalValue = nodeResult.getValue();
		OpcUaDataType dataType = nodeResult.getDataType();

		// 원본 값이 null인 경우 그대로 반환
		if (originalValue == null) {
			log.debug("노드 데이터가 null입니다. NodeId: {}", nodeResult.getNodeId());
			return null;
		}

		// 데이터 타입에 따른 변환 처리
		switch (dataType) {
			case BOOLEAN:
				// boolean을 integer로 변환 (true: 1, false: 0)
				if (originalValue instanceof Boolean boolValue) {
					Integer convertedValue = boolValue
											 ? 1
											 : 0;
					log.debug("Boolean 값을 Integer로 변환했습니다. NodeId: {}, 원본값: {}, 변환값: {}",
						nodeResult.getNodeId(),
						originalValue,
						convertedValue);
					return convertedValue;
				}
				break;

			case STRING:
				// 문자열 타입은 그대로 반환
				log.debug("String 타입 데이터를 반환합니다. NodeId: {}, 값: {}", nodeResult.getNodeId(), originalValue);
				return originalValue;

			case SBYTE:
			case BYTE:
			case INT16:
			case UINT16:
			case INT32:
			case UINT32:
			case INT64:
			case UINT64:
				// 정수형 타입들은 그대로 반환
				log.debug("정수형 데이터 타입 {}를 반환합니다. NodeId: {}, 값: {}",
					dataType.getDisplayName(),
					nodeResult.getNodeId(),
					originalValue);
				return originalValue;

			case FLOAT:
			case DOUBLE:
				// 실수형 타입들은 그대로 반환
				log.debug("실수형 데이터 타입 {}를 반환합니다. NodeId: {}, 값: {}",
					dataType.getDisplayName(),
					nodeResult.getNodeId(),
					originalValue);
				return originalValue;

			case DATE_TIME:
			case GUID:
			case BYTE_STRING:
			case XML_ELEMENT:
			case NODE_ID:
			case EXPANDED_NODE_ID:
			case STATUS_CODE:
			case QUALIFIED_NAME:
			case LOCALIZED_TEXT:
			case EXTENSION_OBJECT:
			case DATA_VALUE:
			case VARIANT:
			case DIAGNOSTIC_INFO:
			case UNKNOWN:
			case CUSTOM:
			default:
				// 지원하지 않는 타입들에 대해 예외 발생
				String errorMessage = String.format("지원하지 않는 데이터 타입입니다. NodeId: %s, 데이터타입: %s, 값: %s",
					nodeResult.getNodeId(),
					dataType.getDisplayName(),
					originalValue);
				log.error(errorMessage);
				throw new UnsupportedOperationException(errorMessage);
		}

		// 예상치 못한 경우 예외 발생
		String errorMessage = String.format("데이터 변환 중 예상치 못한 상황이 발생했습니다. NodeId: %s, 데이터타입: %s, 값: %s",
			nodeResult.getNodeId(),
			dataType.getDisplayName(),
			originalValue);
		log.error(errorMessage);
		throw new UnsupportedOperationException(errorMessage);
	}

	record TargetNode(String nodeId, String parameterName, Long parameterId) {
	}
}
