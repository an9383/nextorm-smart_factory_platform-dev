package com.nextorm.collector.collector.opcua;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OpcUaClientWrapper {
	private final String endpointUrl;

	private OpcUaClient client;
	private boolean isConnected = false;

	/**
	 * OPC UA 클라이언트 초기화
	 *
	 * @param endpointUrl OPC UA 서버 엔드포인트 URL
	 */
	public OpcUaClientWrapper(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	/**
	 * OPC UA 서버에 연결
	 *
	 * @return 연결 성공 여부
	 */
	public boolean connect() {
		try {
			log.info("OPC UA 서버에 연결 시도: {}", endpointUrl);

			// 엔드포인트 검색
			List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl)
																 .get();

			// 보안 정책이 None인 엔드포인트 선택
			EndpointDescription endpoint = endpoints.stream()
													.filter(e -> e.getSecurityPolicyUri()
																  .equals(SecurityPolicy.None.getUri()))
													.findFirst()
													.orElseThrow(() -> new RuntimeException("적절한 엔드포인트를 찾을 수 없습니다"));

			// 호스트 IP를 엔드포인트 URL에 업데이트
			String hostIp = EndpointUtil.getHost(endpointUrl);
			endpoint = EndpointUtil.updateUrl(endpoint, hostIp);

			OpcUaClientConfig config = OpcUaClientConfig.builder()
														.setApplicationName(LocalizedText.english("OPC Node Poller"))
														.setApplicationUri("urn:nextorm:opcnodepoller")
														.setEndpoint(endpoint)
														.build();

			// 클라이언트 생성 및 연결
			client = OpcUaClient.create(config);
			client.connect()
				  .get();

			isConnected = true;
			log.info("OPC UA 서버 연결 성공");
			return true;

		} catch (Exception e) {
			log.error("OPC UA 서버 연결 실패: {}", e.getMessage());
			isConnected = false;
			return false;
		}
	}

	/**
	 * OPC UA 서버 연결 해제
	 */
	public void disconnect() {
		if (client != null && isConnected) {
			try {
				client.disconnect()
					  .get();
				log.info("OPC UA 서버 연결 해제 완료");
			} catch (Exception e) {
				log.error("OPC UA 서버 연결 해제 중 오류 발생: {}", e.getMessage());
			} finally {
				isConnected = false;
			}
		}
	}

	public NodesReadResult readNodesData(List<String> nodeIds) {
		try {

			CompletableFuture<List<DataValue>> results = client.readValues(0,
				TimestampsToReturn.Both,
				nodeIds.stream()
					   .map(NodeId::parse)
					   .toList());

			List<DataValue> dataValues = results.get();
			if (nodeIds.size() != dataValues.size()) {
				log.error("노드 데이터 조회 실패 - 요청한 노드 수와 응답한 노드 수가 일치하지 않습니다. 요청: {}, 응답: {}",
					nodeIds.size(),
					dataValues.size());
				return NodesReadResult.fail();
			}

			List<NodeResult> nodeResults = new ArrayList<>();
			for (int i = 0; i < nodeIds.size(); i++) {
				String nodeId = nodeIds.get(i);
				DataValue dataValue = dataValues.get(i);

				StatusCode statusCode = dataValue.getStatusCode();
				if (statusCode.isGood()) {
					nodeResults.add(NodeResult.success(nodeId, dataValue));
				} else {
					log.warn("노드 데이터 조회 실패 - NodeId: {}, 상태 코드: {}", nodeId, dataValue.getStatusCode());
					nodeResults.add(NodeResult.fail(nodeId));
				}
			}
			return NodesReadResult.success(nodeResults);

		} catch (Exception e) {
			log.error("노드 데이터 조회 중 오류 발생 - NodeId: {}, 오류: {}", nodeIds, e.getMessage());
			return NodesReadResult.fail();
		}
	}

	/**
	 * 연결 상태 확인
	 *
	 * @return 연결 상태
	 */
	public boolean isConnected() {
		return isConnected;
	}

	@Getter
	@ToString
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class NodesReadResult {
		private boolean success;
		private List<NodeResult> nodes;

		static NodesReadResult fail() {
			return new NodesReadResult(false, null);
		}

		static NodesReadResult success(List<NodeResult> nodeResults) {
			return new NodesReadResult(true, nodeResults);
		}
	}

	@Getter
	@ToString
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class NodeResult {
		private boolean success;
		private String nodeId;
		private Object value;
		private OpcUaDataType dataType;
		private String customTypeName; // 커스텀 타입의 경우 상세 이름 저장
		private LocalDateTime sourceTime;
		private LocalDateTime serverTime;

		static NodeResult fail(String nodeId) {
			return new NodeResult(false, nodeId, null, null, null, null, null);
		}

		static NodeResult success(
			String nodeId,
			DataValue dataValue
		) {
			Object value = dataValue.getValue()
									.getValue();

			OpcUaDataTypeResult dataTypeResult = parseDataType(dataValue);

			ZoneId zoneId = ZoneId.systemDefault();
			LocalDateTime sourceTime = LocalDateTime.ofInstant(dataValue.getSourceTime()
																		.getJavaInstant(), zoneId);
			LocalDateTime serverTime = LocalDateTime.ofInstant(dataValue.getServerTime()
																		.getJavaInstant(), zoneId);

			return new NodeResult(true,
				nodeId,
				value,
				dataTypeResult.dataType,
				dataTypeResult.customTypeName,
				sourceTime,
				serverTime);
		}

		/**
		 * 데이터 타입 파싱 결과를 담는 내부 클래스
		 */
		private static class OpcUaDataTypeResult {
			final OpcUaDataType dataType;
			final String customTypeName;

			OpcUaDataTypeResult(
				OpcUaDataType dataType,
				String customTypeName
			) {
				this.dataType = dataType;
				this.customTypeName = customTypeName;
			}
		}

		/**
		 * OPC UA 데이터 타입을 Enum 형태로 변환
		 */
		private static OpcUaDataTypeResult parseDataType(DataValue dataValue) {
			Optional<ExpandedNodeId> dataTypeOpt = dataValue.getValue()
															.getDataType();
			if (dataTypeOpt.isEmpty()) {
				return new OpcUaDataTypeResult(OpcUaDataType.UNKNOWN, null);
			}

			ExpandedNodeId dataType = dataTypeOpt.get();
			if (dataType.getIdentifier() instanceof Number identifier) {
				int typeId = identifier.intValue();

				OpcUaDataType opcDataType = OpcUaDataType.fromTypeId(typeId);
				if (opcDataType == OpcUaDataType.CUSTOM) {
					// 커스텀 타입의 경우 상세 이름 생성
					return new OpcUaDataTypeResult(opcDataType, OpcUaDataType.getCustomTypeName(typeId));
				}

				return new OpcUaDataTypeResult(opcDataType, null);
			}

			// Number가 아닌 경우 Unknown으로 처리
			return new OpcUaDataTypeResult(OpcUaDataType.UNKNOWN, dataType.toString());
		}
	}
}
