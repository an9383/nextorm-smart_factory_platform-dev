package com.nextorm.apc.alertchannel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.TextObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 메시지 구성은 아래의 페이지를 참고하여 작업
 * https://app.slack.com/block-kit-builder/
 */
@Slf4j
@Component
public class SlackNotifyChannel implements NotifyChannel {
	private final String token;
	private final String channel;

	private final ObjectMapper objectMapper;

	public SlackNotifyChannel(
		@Value("${apc.notify.slack.token}") String token,
		@Value("${apc.notify.slack.channel}") String channel,
		ObjectMapper objectMapper
	) {
		this.token = token;
		this.channel = channel;
		this.objectMapper = objectMapper;
	}

	@Override
	public void notify(NotifyMessage notifyMessage) {
		Slack slack = Slack.getInstance();
		MethodsClient methods = slack.methods(token);

		ChatPostMessageRequest request = ChatPostMessageRequest.builder()
															   .channel(channel)
															   .text("APC 결과 알림")    // slack api warning 제거를 위해 text 필드 추가
															   .blocks(alertMessageToLayoutBlocks(notifyMessage))
															   .build();

		try {
			ChatPostMessageResponse response = methods.chatPostMessage(request);
			if (!response.isOk()) {
				log.info("Slack alert failed. reason: {}", response.getError());
			}
		} catch (IOException | SlackApiException e) {
			log.error("Slack alert failed. message: {}", notifyMessage, e);
		}
	}

	private List<LayoutBlock> alertMessageToLayoutBlocks(NotifyMessage notifyMessage) {
		SectionBlock infoSection = createInfoSection(notifyMessage);
		SectionBlock dataSection = createDataSectionBlock(notifyMessage);
		DividerBlock div = DividerBlock.builder()
									   .build();

		return List.of(infoSection, dataSection, div);
	}

	private SectionBlock createInfoSection(NotifyMessage notifyMessage) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String requestTimeString = notifyMessage.requestTime()
												.format(formatter);

		TextObject title = createMarkdownTextObject("*APC 결과 알림*");
		List<TextObject> infoFields = List.of(createMarkdownTextObject("*상태:* " + notifyMessage.status()),
			createMarkdownTextObject(" "),
			createMarkdownTextObject("*요청 시간:* " + requestTimeString),
			createMarkdownTextObject(" "),
			createMarkdownTextObject("*ApcRequestId:* " + notifyMessage.apcRequestId()));

		return SectionBlock.builder()
						   .text(title)
						   .fields(infoFields)
						   .build();
	}

	private SectionBlock createDataSectionBlock(NotifyMessage notifyMessage) {
		String dataString = """
			```
			요청 및 결과 데이터
			
			source: %s
			
			result: %s
			```
			""".formatted(notifyMessage.sourceJsonString(), resultToJsonString(notifyMessage.results()));

		return SectionBlock.builder()
						   .text(createMarkdownTextObject(dataString))
						   .build();
	}

	private TextObject createMarkdownTextObject(String text) {
		return MarkdownTextObject.builder()
								 .text(text)
								 .build();
	}

	private String resultToJsonString(Map<String, Object> resultMap) {
		if (resultMap == null) {
			return "";
		}
		try {
			return objectMapper.writeValueAsString(resultMap);
		} catch (JsonProcessingException e) {
			log.error("Failed to convert result map to json string. result map: {}", resultMap, e);
			return "결과를 문자열로 변환하는데 실패했습니다.";
		}
	}
}
