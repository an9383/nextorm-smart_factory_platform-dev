package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.common.db.entity.ai.ParameterDetail;
import com.nextorm.processor.aimodel.AiModelData;
import com.nextorm.processor.aimodel.AiModelQueryService;
import com.nextorm.processor.config.AiServerProperties;
import com.nextorm.processor.dto.AiInferenceRequest;
import com.nextorm.processor.dto.AiInferenceResponse;
import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class GetAiModelPrediction implements VpCalculateExecutor {
	private final AiServerProperties aiServerProperties;
	private final AiModelQueryService aiModelQueryService;
	private final RestTemplate restTemplate;

	public GetAiModelPrediction(
		AiServerProperties aiServerProperties,
		AiModelQueryService aiModelQueryService,
		RestTemplate restTemplate
	) {
		this.aiServerProperties = aiServerProperties;
		this.aiModelQueryService = aiModelQueryService;
		this.restTemplate = restTemplate;
	}

	@Override
	public String getScript() {
		return """
			function getAiModelPrediction(aiModelId) {
				const xParameterIds = self_getAiModelPrediction.getXParameterIds(aiModelId);
				if (xParameterIds == null) {
					return null;
				}
				const xParameterValues = xParameterIds.map(id => {
					return %s[id];
				});
				return self_getAiModelPrediction.fetchInference(aiModelId, xParameterValues);
			}
			""".formatted(ScriptEngine.CONTEXT_CONTAINER_NAME);
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_getAiModelPrediction", this);
	}

	public List<Long> getXParameterIds(String aiModelId) {
		AiModelData aiModel = aiModelQueryService.findById(Long.parseLong(aiModelId));
		if (aiModel == null) {
			return null;
		}

		return aiModel.getParameterDetails()
					  .stream()
					  .map(ParameterDetail::getId)
					  .toList();
	}

	public Double fetchInference(
		String aiModelId,
		List<Double> xParameterValues
	) {
		return inferLinearRegression(Long.parseLong(aiModelId), xParameterValues);
	}

	public Double inferLinearRegression(
		Long modelId,
		List<Double> featureValues
	) {
		String url = aiServerProperties.getUrl() + "/api/linear-regression/inference";

		AiInferenceRequest request = AiInferenceRequest.builder()
													   .site(aiServerProperties.getSiteId())
													   .modelId(modelId.toString())
													   .featureValues(featureValues)
													   .build();

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<AiInferenceRequest> httpEntity = new HttpEntity<>(request, headers);

			ResponseEntity<AiInferenceResponse> response = restTemplate.exchange(url,
				HttpMethod.POST,
				httpEntity,
				new ParameterizedTypeReference<AiInferenceResponse>() {
				});

			if (response.getStatusCode()
						.is2xxSuccessful()) {
				AiInferenceResponse aiResponse = response.getBody();
				log.debug("AI Server inference response: {}", aiResponse);

				if (aiResponse != null && aiResponse.isSuccess()) {
					return aiResponse.getData();
				} else {
					log.error(this.predictionErrorMessage(modelId));
					return null;
				}
			} else {
				log.error(this.aiInferenceResponseErrorMessage(modelId, response.getStatusCode()));
				return null;
			}

		} catch (Exception exception) {
			log.error(this.aiServerConnectionErrorMessage(url, exception.getMessage()));
			return null;
		}
	}

	private String predictionErrorMessage(Long modelId) {

		return String.format("AI 모델 예측에 실패했습니다. 모델Id : %s", modelId);
	}

	private String aiInferenceResponseErrorMessage(
		Long modelId,
		HttpStatusCode statusCode
	) {
		return String.format("AI 서버 응답에 오류가 있습니다. 모델Id : %s, 응답코드 : %s", modelId, statusCode);
	}

	private String aiServerConnectionErrorMessage(
		String serverUrl,
		String errorMessage
	) {
		return String.format("AI 서버 통신 오류가 발생했습니다. 요청 URL : %s, 에러 메세지: %s", serverUrl, errorMessage);
	}
}
