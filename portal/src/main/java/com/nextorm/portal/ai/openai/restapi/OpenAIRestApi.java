package com.nextorm.portal.ai.openai.restapi;

import com.nextorm.portal.ai.openai.restapi.dto.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OpenAIRestApi {

	public static final String BASE_URL = "https://api.openai.com/";

	@POST("v1/threads/runs")
	Call<RunResponseDto> createThreadAndRun(@Body ThreadCreateRequestDto threadCreateRequest);

	@POST("v1/threads/{threadId}/runs")
	Call<RunResponseDto> createRun(
		@Path("threadId") String threadId,
		@Body RunCreateRequestDto runCreateRequest
	);

	@GET("v1/threads/{threadId}/runs/{runId}")
	Call<RunResponseDto> retrieveRun(
		@Path("threadId") String threadId,
		@Path("runId") String runId
	);

	@POST("v1/threads/{threadId}/messages")
	Call<MessageResponseDto> sendMessage(
		@Path("threadId") String threadId,
		@Body MessageSendRequestDto messageSendRequest
	);

	@GET("v1/threads/{threadId}/messages")
	Call<MessageListResponseDto> getMessages(@Path("threadId") String threadId);

	@POST("/v1/threads/{threadId}/runs/{runId}/submit_tool_outputs")
	Call<RunResponseDto> submitToolOutputs(
		@Path("threadId") String threadId,
		@Path("runId") String runId,
		@Body ToolOutputRequestDto toolOutputRequest
	);
}