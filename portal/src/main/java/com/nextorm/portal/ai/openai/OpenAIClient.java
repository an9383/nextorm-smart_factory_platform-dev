package com.nextorm.portal.ai.openai;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nextorm.portal.ai.openai.constant.Status;
import com.nextorm.portal.ai.openai.dto.RecievedMessageDto;
import com.nextorm.portal.ai.openai.restapi.OpenAIRestApi;
import com.nextorm.portal.ai.openai.restapi.dto.*;
import com.nextorm.portal.config.localdatetime.LocalDateTimeToIsoSerializer;
import lombok.Builder;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenAIClient {
	private static final Long RUN_RETRIEVE_INTERVAL = 500L;
	private final OpenAIRestApi openAIRestApi;
	private final String assistantId;

	private String threadId;

	public ToolOutputHandler toolOutputHandler;

	public interface ToolOutputHandler {
		OutputResult generateOutput(
			String function,
			Map<String, Object> arguments
		);

		@Builder
		@Data
		public static class OutputResult {
			private boolean isExtraData;
			private Object result;
		}
	}

	@Builder
	private OpenAIClient(
		String apiKey,
		String assistantId,
		String threadId,
		ToolOutputHandler toolOutputHandler
	) {
		this.assistantId = assistantId;
		this.threadId = threadId;
		this.toolOutputHandler = toolOutputHandler;

		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		httpClient.addInterceptor(loggingInterceptor)
				  .addInterceptor(chain -> {
					  Request request = chain.request()
											 .newBuilder()
											 .addHeader("Content-Type", "application/json")
											 .addHeader("Authorization", String.format("Bearer %s", apiKey))
											 .addHeader("OpenAI-Beta", "assistants=v1")
											 .build();
					  return chain.proceed(request);
				  });
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		SimpleModule module = new SimpleModule();

		module.addDeserializer(Status.class, new Status.StatusJsonDeSerializer())
			  .addDeserializer(Map.class, new JsonDeserializer<Map<String, Object>>() {
				  @Override
				  public Map<String, Object> deserialize(
					  JsonParser jsonParser,
					  DeserializationContext deserializationContext
				  ) throws IOException {
					  ObjectMapper mapper = new ObjectMapper();
					  String json = jsonParser.getText(); // JSON 문자열을 가져옵니다.
					  return mapper.readValue(json, Map.class); // 문자열을 Map으로 변환합니다.
				  }
			  });
		objectMapper.registerModule(module);
		Retrofit retrofit = new Retrofit.Builder().baseUrl(OpenAIRestApi.BASE_URL)
												  .addConverterFactory(JacksonConverterFactory.create(objectMapper))
												  .client(httpClient.build())
												  .build();
		this.openAIRestApi = retrofit.create(OpenAIRestApi.class);
	}

	public static OpenAIClientBuilder builder(
		String apiKey,
		String assistantId
	) {
		return new OpenAIClientBuilder().apiKey(apiKey)
										.assistantId(assistantId);
	}

	private RunResponseDto createThread(String message) throws IOException {
		ThreadCreateRequestDto threadCreateRequest = ThreadCreateRequestDto.builder(this.assistantId, message)
																		   .build();
		Call<RunResponseDto> callCreateThread = this.openAIRestApi.createThreadAndRun(threadCreateRequest);
		Response<RunResponseDto> respCreateThread = callCreateThread.execute();
		this.threadId = respCreateThread.body()
										.getThreadId();
		return respCreateThread.body();
	}

	public RecievedMessageDto sendMessage(String message) {
		try {
			RunResponseDto runResponse = null;
			String sentMessageId = null;
			if (this.threadId == null) {
				runResponse = this.createThread(message);
			} else {
				MessageSendRequestDto messageSendRequest = MessageSendRequestDto.builder()
																				.content(message)
																				.build();
				Call<MessageResponseDto> callSendMessage = this.openAIRestApi.sendMessage(this.threadId,
					messageSendRequest);
				Response<MessageResponseDto> respSendMessage = callSendMessage.execute();

				sentMessageId = respSendMessage.body()
											   .getId();

				RunCreateRequestDto runCreateReques = RunCreateRequestDto.builder()
																		 .assistantId(assistantId)
																		 .build();
				Call<RunResponseDto> callCreateRun = this.openAIRestApi.createRun(this.threadId, runCreateReques);
				Response<RunResponseDto> respRunCreate = callCreateRun.execute();
				runResponse = respRunCreate.body();
			}
			return this.receiveMessages(this.threadId, runResponse.getId(), sentMessageId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("");
		}
	}

	private RecievedMessageDto receiveMessages(
		String threadId,
		String runId,
		String sentMessageId
	) throws InterruptedException, IOException {
		RecievedMessageDto.RecievedMessageDtoBuilder recievedMessageBuilder = RecievedMessageDto.builder();
		List<RecievedMessageDto.ExtraData> extraDatas = new ArrayList<>();
		while (true) {
			Call<RunResponseDto> callRetrieveRun = this.openAIRestApi.retrieveRun(threadId, runId);
			Response<RunResponseDto> respRetrieveRun = callRetrieveRun.execute();

			if (respRetrieveRun.isSuccessful()) {
				Status status = respRetrieveRun.body()
											   .getStatus();
				if (status == Status.COMPLETED) {
					Call<MessageListResponseDto> callMessage = this.openAIRestApi.getMessages(threadId);
					Response<MessageListResponseDto> respMessage = callMessage.execute();

					List<RecievedMessageDto.Message> messages = respMessage.body()
																		   .getData()
																		   .stream()
																		   .takeWhile(msg -> !msg.getId()
																								 .equals(sentMessageId))
																		   .filter(msg -> !"user".equals(msg.getRole()))
																		   .map(msg -> {
																			   String message = String.join("",
																				   msg.getContent()
																					  .stream()
																					  .map(content -> content.getText()
																											 .getValue())
																					  .toList());
																			   return RecievedMessageDto.Message.builder()
																												.id(msg.getId())
																												.message(
																													message)
																												.build();
																		   })
																		   .collect(Collectors.toList());
					Collections.reverse(messages);
					recievedMessageBuilder.threadId(threadId);
					recievedMessageBuilder.messages(messages);
					break;
				} else if (status == Status.REQUIRES_ACTION && toolOutputHandler != null) {
					List<RunResponseDto.ToolCall> toolCalls = respRetrieveRun.body()
																			 .getRequiredAction()
																			 .getSubmitToolOutputs()
																			 .getToolCalls();
					List<ToolOutputRequestDto.ToolOutput> toolOutputs = toolCalls.stream()
																				 .map(tool -> {
																					 String toolId = tool.getId();
																					 ToolOutputRequestDto.ToolOutput.ToolOutputBuilder toolOutputBuilder = ToolOutputRequestDto.ToolOutput.builder()
																																														  .toolCallId(
																																															  toolId);
																					 if (tool.getFunction() != null) {
																						 String function = tool.getFunction()
																											   .getName();
																						 Map<String, Object> arguments = tool.getFunction()
																															 .getArguments();
																						 ToolOutputHandler.OutputResult outputResult = toolOutputHandler.generateOutput(
																							 function,
																							 arguments);

																						 if (outputResult.isExtraData()) {
																							 extraDatas.add(
																								 RecievedMessageDto.ExtraData.builder()
																															 .type(
																																 RecievedMessageDto.ExtraData.Type.LINE_CHART)
																															 .data(
																																 outputResult.getResult())
																															 .build());
																						 }

																						 ObjectMapper objectMapper = new ObjectMapper();
																						 SimpleModule module = new SimpleModule();
																						 module.addSerializer(
																							 LocalDateTime.class,
																							 new LocalDateTimeToIsoSerializer());
																						 objectMapper.registerModule(
																							 module);
																						 try {
																							 toolOutputBuilder.output(
																								 objectMapper.writeValueAsString(
																									 outputResult.getResult()));
																						 } catch (
																							 JsonProcessingException e) {
																							 throw new RuntimeException(
																								 e);
																						 }
																					 }
																					 return toolOutputBuilder.build();
																				 })
																				 .toList();
					ToolOutputRequestDto toolOutputRequest = ToolOutputRequestDto.builder()
																				 .toolOutputs(toolOutputs)
																				 .build();
					Call call = openAIRestApi.submitToolOutputs(threadId, runId, toolOutputRequest);
					Response reponse = call.execute();
				}
			}
			Thread.sleep(RUN_RETRIEVE_INTERVAL);
		}
		return recievedMessageBuilder.extraDatas(extraDatas)
									 .build();
	}
}

