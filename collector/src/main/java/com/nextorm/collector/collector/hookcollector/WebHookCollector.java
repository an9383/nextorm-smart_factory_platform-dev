package com.nextorm.collector.collector.hookcollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.collector.Collector;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.collector.sender.Sender;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.util.function.Consumer;

@Slf4j
public class WebHookCollector implements Collector {
	private final DataCollectPlan plan;
	private final Sender sender;

	private final Tomcat server;

	public WebHookCollector(
		DataCollectPlan config,
		ObjectMapper objectMapper,
		Sender sender
	) {
		this.plan = config;
		this.sender = sender;

		int port = Integer.parseInt(plan.getCollectorArguments()
										.get("port")
										.toString());

		this.server = new Tomcat();
		Connector connector = new Connector();
		connector.setPort(port);
		server.setConnector(connector);

		Context context = server.addContext("/api", null);
		server.addServlet(context.getPath(),
			HookServlet.SERVLET_NAME,
			new HookServlet(plan, objectMapper, consumeSendMessage()));
		context.addServletMappingDecoded(HookServlet.PATH, HookServlet.SERVLET_NAME);

		connector.addLifecycleListener(event -> {
			switch (event.getType()
						 .toUpperCase()) {
				case "START":
					log.info("WebHook Collector START. port: {}", port);
					break;
				case "STOP":
					log.info("WebHook Collector STOP. port: {}", port);
					break;
				default:
					break;
			}
		});
	}

	private Consumer<SendMessage> consumeSendMessage() {
		return sendMessage -> sender.send(plan.getTopic(), sendMessage);
	}

	@Override
	public void run() {
		try {
			server.start();
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void shutdown() {
		try {
			log.info("WebHook Collector Server STOP. DcpId: {}", plan.getDcpId());
			this.server.stop();
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DataCollectPlan getConfig() {
		return plan;
	}
}
