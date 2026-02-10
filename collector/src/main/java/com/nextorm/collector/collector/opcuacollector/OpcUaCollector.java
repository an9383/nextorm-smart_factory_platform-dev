package com.nextorm.collector.collector.opcuacollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.collector.Collector;
import com.nextorm.collector.collector.opcuacollector.connectivity.DEFINE_OPCUA_COMMAND;
import com.nextorm.collector.collector.opcuacollector.connectivity.OpcUaMessageRecvHandler;
import com.nextorm.collector.collector.opcuacollector.opcuaclient.ClientRunner;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OpcUaCollector implements Collector, OpcUaMessageRecvHandler {

	private final DataCollectPlan config;
	private final Sender sender;
	private final ObjectMapper objectMapper;

	private String ip;
	private int port;
	private String context = "";
	private String xmlFileName;
	private boolean isSubscription;

	private OpcUaCollectorHandler handler = null;
	private ClientRunner clientRunner = null;
	private AtomicBoolean running = new AtomicBoolean(true);

	public OpcUaCollector(
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
		xmlFileName = config.getCollectorArguments()
							.get("xmlFileName")
							.toString();
		isSubscription = Boolean.parseBoolean(config.getCollectorArguments()
													.get("isSubscription")
													.toString());
	}

	@Override
	public void run() {
		try {
			clientRunner = new ClientRunner();
			handler = new OpcUaCollectorHandler(ip,
				port,
				context,
				"",
				"",
				this,
				null,
				xmlFileName,
				config.getToolName());
			clientRunner.initialized(handler, config.getToolName());

			boolean connected = clientRunner.run(ip, port);
			if (!connected) {
				log.error("OPC UA connect failed => toolName={}", config.getToolName());
				return;
			}
			log.info("OPC UA connected (long-term). toolId={}", config.getToolId());

			while (running.get() && !Thread.currentThread()
										   .isInterrupted()) {
				if (isSubscription) {
					// --- Subscription 모드 ---
					// (1) "최근 변경된 파라미터 목록" 가져오기
					Set<String> changedSet = handler.consumeChangedParams();

					// (2) changedSet 비어 있지 않으면 "바뀐 파라미터만" 전송
					if (!changedSet.isEmpty()) {
						log.debug("changedSet => {}", changedSet);

						// subscriptionData (전체 map)
						Map<String, Object> fullData = handler.getSubscriptionData();

						// 부분전송: changedSet 항목만 골라서 partialData 구성
						Map<String, Object> partialData = new HashMap<>();
						for (String param : changedSet) {
							Object val = fullData.get(param);
							if (val != null) {
								partialData.put(param, val);
							}
						}

						if (!partialData.isEmpty()) {
							// 부분 전송이므로, paramValuesToMessage()의 전체 파라미터 개수 검증은 피하거나 별도 메서드를 써야 함.
							// 아래처럼 "부분 전송" 메서드를 새로 만듦
							SendMessage msg = paramValuesToPartialMessage(partialData);
							sender.send(config.getTopic(), msg);
						}
					}

				} else {
					// --- Trace 모드 ---
					// 5초마다(혹은 dataInterval마다) 무조건 전송
					Map<String, Object> traceData = handler.getTraceData();
					if (!traceData.isEmpty()) {
						// 전체 파라미터 개수와 동일해야 한다면 paramValuesToMessage() 써도 됨
						SendMessage msg = paramValuesToMessage(traceData);
						sender.send(config.getTopic(), msg);
					}
				}

				// 대기
				Thread.sleep(config.getDataInterval() * 1000L);
			}
		} catch (InterruptedException e) {
			Thread.currentThread()
				  .interrupt();
		} catch (Exception e) {
			log.error("OpcUaCollector run error => {}", e.getMessage(), e);
		} finally {
			if (clientRunner != null) {
				clientRunner.quit(clientRunner.getClient());
			}
			log.info("OpcUaCollector finished => toolId={}", config.getToolId());
		}
	}

	/**
	 * "전체 파라미터"가 아니라 "부분 전송"을 허용하기 위한 메서드
	 */
	private SendMessage paramValuesToPartialMessage(Map<String, Object> partialData) {
		// 여기서는 "전체 개수" 검증 로직을 제거 or 별도 처리
		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), partialData);
	}

	/**
	 * 기존 전체 파라미터 전송 로직
	 */
	private SendMessage paramValuesToMessage(Map<String, Object> paramsValueMap) {
		int collectParameterSize = config.getCollectParameters()
										 .size();
		if (paramsValueMap.size() != collectParameterSize) {
			throw new IllegalArgumentException("파라미터 개수 불일치!");
		}
		return SendMessage.createMergedMetadataMessage(config, System.currentTimeMillis(), paramsValueMap);
	}

	@Override
	public void shutdown() {
		running.set(false);
	}

	@Override
	public DataCollectPlan getConfig() {
		return config;
	}

	/* ===== OpcUaMessageRecvHandler 구현부 ===== */
	@Override
	public void onMessage(
		DEFINE_OPCUA_COMMAND command,
		String datas,
		String ip,
		String toolId
	) {
		// 필요 시 사용
	}

	@Override
	public void onDisconnected(
		String p_ip,
		int p_port,
		String toolName
	) {
		log.info("onDisconnected => toolName={}, {}:{}", toolName, p_ip, p_port);
	}

	@Override
	public void onConnected(
		OpcUaCollectorHandler handler,
		String hostAddress,
		int port,
		String toolName
	) {
		log.info("onConnected => toolName={}, {}:{}", toolName, hostAddress, port);
	}

	@Override
	public boolean isTryConnection(String toolName) {
		return false;
	}

	@Override
	public void putOpcConnectionOn(String toolName) {
	}

	@Override
	public void putOpcConnectionOff(String toolName) {
	}

	@Override
	public boolean isOpcDisConnection(String toolName) {
		return false;
	}
}
