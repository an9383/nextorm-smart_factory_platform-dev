package com.nextorm.portal.entity.processoptimization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.db.util.ListGenericToJsonConverter;

import java.util.List;

public class ListOptimizationParametersToJsonStringConverter
	extends ListGenericToJsonConverter<OptimizationParameters> {

	public ListOptimizationParametersToJsonStringConverter(ObjectMapper objectMapper) {
		super(objectMapper, new TypeReference<List<OptimizationParameters>>() {
		});
	}
}
