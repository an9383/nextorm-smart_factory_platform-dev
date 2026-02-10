package com.nextorm.collector.collector.opcuacollector;

import com.nextorm.collector.collector.opcuacollector.connectivity.OpcUaMessageRecvHandler;
import com.nextorm.collector.collector.opcuacollector.data.LoadOpcUaNameSpace;
import com.nextorm.collector.collector.opcuacollector.data.OpcUaData;
import com.nextorm.collector.collector.opcuacollector.opcuaclient.Client;
import com.nextorm.collector.collector.opcuacollector.opcuaclient.ClientRunner;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DataChangeTrigger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DeadbandType;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.DataChangeFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Slf4j
public class OpcUaCollectorHandler implements Client {

	private static final String LOG_PREFIX = "OpcUaCollectorHandler";

	private String ip;
	private int port;
	private String context;
	private String userName;
	private String password;
	private String xmlFileName;
	private String toolId;
	private OpcUaClient opcUaClient;
	private OpcUaMessageRecvHandler messageReceiver;

	private final AtomicLong clientHandles = new AtomicLong(1L);

	// subscriptionData => "파라미터명" -> "현재 값"
	private final Map<String, Object> subscriptionData = new ConcurrentHashMap<>();

	// changedParams => "최근 콜백에서 바뀐 파라미터명"
	// Collector가 이 목록을 소비(consume)할 때마다 비워짐
	private final Set<String> changedParams = Collections.synchronizedSet(new HashSet<>());

	// subscriptionList 등 XML 로딩용
	private ArrayList<OpcUaData> subscriptionList;

	// nodeId -> paramName 매핑
	private final Map<String, String> nodeIdToParamNameMap = new HashMap<>();

	public OpcUaCollectorHandler(
		String ip,
		int port,
		String context,
		String userName,
		String password,
		OpcUaMessageRecvHandler receiver,
		List<?> paramDataTypeList,
		String xmlFileName,
		String toolName
	) {
		this.ip = ip;
		this.port = port;
		this.context = context;
		this.userName = userName;
		this.password = password;
		this.messageReceiver = receiver;
		this.xmlFileName = xmlFileName;
		this.toolId = toolName;

		// 1) XML 파싱 (subscriptionList)
		LoadOpcUaNameSpace loader = new LoadOpcUaNameSpace();
		loader.initilaize(this.xmlFileName);
		this.subscriptionList = loader.loadToolConnectorList("/ToolConnector/OpcUaNameSpace/Subscription",
			"/ToolConnector/OpcUaNameSpace/Subscription/UaMonitoredItem");

		// 2) nodeId -> paramName 맵 생성
		createNodeIdMap();
	}

	private void createNodeIdMap() {
		for (OpcUaData data : subscriptionList) {
			String nodeIdStr = data.uaMonitoredItem.getNodeId();
			String paramName = data.uaMonitoredItem.getBrowseName();
			nodeIdToParamNameMap.put(nodeIdStr, paramName);
		}
	}

	// 구독 콜백에서 바뀐 paramName을 기록
	private void onSubscriptionValue(
		UaMonitoredItem item,
		org.eclipse.milo.opcua.stack.core.types.builtin.DataValue value
	) {
		try {
			String nodeIdStr = item.getReadValueId()
								   .getNodeId()
								   .getIdentifier()
								   .toString();
			Object val = value.getValue()
							  .getValue();

			// nodeId -> paramName
			String paramName = nodeIdToParamNameMap.get(nodeIdStr);
			if (paramName != null) {
				// subscriptionData 업데이트
				subscriptionData.put(paramName, val);

				// 변경된 파라미터 목록에 추가
				changedParams.add(paramName);

				log.debug("{}: [{}] subCallback => paramName={}, val={}", LOG_PREFIX, toolId, paramName, val);
			} else {
				log.warn("{}: no paramName for nodeId={}", LOG_PREFIX, nodeIdStr);
			}
		} catch (Exception e) {
			log.error("{}: onSubscriptionValue error", LOG_PREFIX, e);
		}
	}

	/**
	 * Collector 스레드에서 주기적으로 호출 -> "최근 변경된 파라미터 목록"만 가져간다
	 * 가져간 뒤 changedParams는 비워짐
	 */
	public Set<String> consumeChangedParams() {
		synchronized (changedParams) {
			if (changedParams.isEmpty()) {
				return Collections.emptySet();
			}
			// 복사본 만들고 clear
			Set<String> changedCopy = new HashSet<>(changedParams);
			changedParams.clear();
			return changedCopy;
		}
	}

	/**
	 * 현재 구독 데이터 맵 (paramName -> value) 전부 반환
	 */
	public Map<String, Object> getSubscriptionData() {
		return new HashMap<>(subscriptionData);
	}

	/**
	 * Trace(폴링) 방식 => nodeIdArray가 없고, subscriptionList를 그대로 사용
	 */
	public Map<String, Object> getTraceData() {
		Map<String, Object> result = new HashMap<>();
		if (opcUaClient == null) {
			return result;
		}
		try {
			for (OpcUaData data : subscriptionList) {
				String nodeIdStr = data.uaMonitoredItem.getNodeId();
				int nsIndex = data.uaMonitoredItem.getNameSpaceIndex();
				NodeId nodeId = new NodeId(nsIndex, Integer.parseInt(nodeIdStr));

				// 변수 노드
				UaNode node = opcUaClient.getAddressSpace()
										 .getNode(nodeId);
				if (node != null && node instanceof UaVariableNode) {
					UaVariableNode varNode = (UaVariableNode)node;
					DataValue dv = varNode.readValue();
					Object val = dv.getValue()
								   .getValue();
					// browseName or nodeId string as key
					result.put(data.uaMonitoredItem.getBrowseName(), val);
				}
			}
		} catch (Exception e) {
			log.error("{}: [{}] getTraceData error => {}", LOG_PREFIX, toolId, e.getMessage(), e);
		}
		return result;
	}

	/* Client 인터페이스 구현 */
	@Override
	public void run(
		ClientRunner clientRun,
		OpcUaClient client
	) {
		try {
			boolean connected = clientRun.opcConnect(client);
			if (!connected) {
				log.info("{}: [{}] OPC connection failed", LOG_PREFIX, toolId);
				return;
			}
			this.opcUaClient = client;

			// 연결 성공
			messageReceiver.onConnected(this, ip, port, toolId);

			// Subscription 생성
			createLongTermSubscription();
		} catch (Exception e) {
			log.warn("{}: run() error => {}", LOG_PREFIX, e.getMessage(), e);
		}
	}

	private void createLongTermSubscription() throws Exception {
		UaSubscription subscription = opcUaClient.getSubscriptionManager()
												 .createSubscription(2000.0)
												 .get();

		List<MonitoredItemCreateRequest> reqList = new ArrayList<>();

		for (OpcUaData data : subscriptionList) {
			String nodeIdStr = data.uaMonitoredItem.getNodeId();
			int nsIndex = data.uaMonitoredItem.getNameSpaceIndex();
			int nodeIdVal = Integer.parseInt(nodeIdStr);

			NodeId nodeId = new NodeId(nsIndex, nodeIdVal);

			ReadValueId readValueId = new ReadValueId(nodeId,
				org.eclipse.milo.opcua.stack.core.AttributeId.Value.uid(),
				null,
				QualifiedName.NULL_VALUE);

			DataChangeFilter filter = new DataChangeFilter(DataChangeTrigger.StatusValue,
				uint(DeadbandType.None.getValue()),
				0.0);
			SerializationContext sContext = opcUaClient.getDynamicSerializationContext();
			ExtensionObject xoFilter = ExtensionObject.encode(sContext, filter);

			MonitoringParameters parameters = new MonitoringParameters(uint(clientHandles.getAndIncrement()),
				1000.0,
				xoFilter,
				uint(10),
				true);

			MonitoredItemCreateRequest req = new MonitoredItemCreateRequest(readValueId,
				MonitoringMode.Reporting,
				parameters);
			reqList.add(req);
		}

		UaSubscription.ItemCreationCallback onItemCreated = (item, id) -> {
			item.setValueConsumer(this::onSubscriptionValue);
		};

		subscription.createMonitoredItems(TimestampsToReturn.Both, reqList, onItemCreated)
					.get();
		log.info("{}: [{}] createLongTermSubscription => {} items", LOG_PREFIX, toolId, reqList.size());
	}

	@Override
	public String getEndpointUrl() {
		return "opc.tcp://" + ip + ":" + port + (context.startsWith("/")
												 ? context
												 : "/" + context);
	}

	@Override
	public IdentityProvider getIdentityProvider() {
		return new AnonymousProvider();
	}

	@Override
	public org.eclipse.milo.opcua.stack.core.security.SecurityPolicy getSecurityPolicy() {
		return org.eclipse.milo.opcua.stack.core.security.SecurityPolicy.None;
	}
}
