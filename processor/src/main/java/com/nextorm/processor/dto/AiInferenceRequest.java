package com.nextorm.processor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInferenceRequest {
	private String site;
	private String modelId;
	private List<Double> featureValues;
}
