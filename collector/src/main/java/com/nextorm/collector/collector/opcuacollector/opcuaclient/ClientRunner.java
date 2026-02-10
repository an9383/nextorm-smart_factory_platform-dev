package com.nextorm.collector.collector.opcuacollector.opcuaclient;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Slf4j
public class ClientRunner {
	private static final String LOG_PREFIX = "ClientRunner";
	protected String applicationName = "OpcUa ToolConnector";
	protected String applicationUri = "urn:local_host:OPCUA_CLIENT";
	private Client client = null;
	private String toolName = null;
	private OpcUaClient opcUaClient = null;

	public void initialized(
		Client client,
		String toolName
	) {
		this.client = client;
		this.toolName = toolName;
	}

	public void quit(OpcUaClient client) {
		try {
			if (client != null) {
				client.disconnect()
					  .get();
				log.info("Client disconnected: {}", toolName);
			}
			// Stack.releaseSharedResources();
		} catch (Exception e) {
			log.warn("quit error: {}", e.getMessage(), e);
		}
	}

	private OpcUaClient createClient(
		String ip,
		int port
	) throws Exception {
		String endpointUrl = "opc.tcp://" + ip + ":" + port;

		List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl)
															 .get();
		EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), ip, port);

		OpcUaClientConfig config = OpcUaClientConfig.builder()
													.setApplicationName(LocalizedText.english("OpcUaToolConnector"))
													.setApplicationUri("urn:local_host:OPCUA_CLIENT")
													.setEndpoint(configPoint)
													.setIdentityProvider(client.getIdentityProvider())
													.setRequestTimeout(uint(60000))
													.build();

		return OpcUaClient.create(config);
	}

	public boolean opcConnect(OpcUaClient client) {
		// synchronous connect
		try {
			client.connect()
				  .get();
		} catch (InterruptedException | ExecutionException e) {
			log.debug("ClientRunner", "[" + this.toolName + "] : opcConnect : " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean run(
		String ip,
		int port
	) {
		boolean result = false;
		try {
			opcUaClient = createClient(ip, port);

			if (opcUaClient != null) {
				client.run(this, opcUaClient);
				result = true;
			}
		} catch (Throwable t) {
			log.error("ClientRunner run error for toolName={}", toolName, t);
		}
		return result;
	}

	public OpcUaClient getClient() {
		return opcUaClient;
	}

}
