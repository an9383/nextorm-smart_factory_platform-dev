package com.nextorm.collector.collector.hookcollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.collector.sender.SendMessage;
import com.nextorm.common.define.collector.DataCollectPlan;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class HookServlet extends HttpServlet {
	public static final String PATH = "/hook/*";
	public static final String SERVLET_NAME = "hook";

	private final DataCollectPlan plan;
	private final ObjectMapper objectMapper;
	private final Consumer<SendMessage> messageConsumer;

	private final Set<String> collectParameterNameSet = new HashSet<>();

	public HookServlet(
		DataCollectPlan plan,
		ObjectMapper objectMapper,
		Consumer<SendMessage> messageConsumer
	) {
		this.plan = plan;
		this.objectMapper = objectMapper;
		this.messageConsumer = messageConsumer;

		this.plan.getCollectParameters()
				 .forEach(it -> collectParameterNameSet.add(it.getName()));
	}

	@Override
	protected void doPost(
		HttpServletRequest req,
		HttpServletResponse resp
	) throws ServletException, IOException {
		String contentType = req.getContentType();
		log.info("Hook 요청이 들어왔습니다. path: {}", req.getRequestURI());
		log.info("Hook 요청이 들어왔습니다. contentType: {}", req.getContentType());

		boolean isJsonContent = MediaType.APPLICATION_JSON.includes(MediaType.valueOf(contentType));
		if (!isJsonContent) {
			flushNotSupportedContentType(resp);
			return;
		}

		Map<String, Object> jsonBody = objectMapper.readValue(req.getReader(), Map.class);
		for (String key : jsonBody.keySet()) {
			if (!collectParameterNameSet.contains(key)) {
				jsonBody.remove(key);
			}
		}

		SendMessage sendMessage = SendMessage.createMergedMetadataMessage(plan, System.currentTimeMillis(), jsonBody);
		messageConsumer.accept(sendMessage);

		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();
		writer.println("{\"message\": \"Hello, this is a JSON response\"}");
		writer.flush();
	}

	private void flushNotSupportedContentType(HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();
		writer.println("{\"message\": \"Not supported content type\"}");
		writer.flush();
	}
}
