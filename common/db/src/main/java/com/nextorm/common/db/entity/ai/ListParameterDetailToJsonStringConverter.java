package com.nextorm.common.db.entity.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.db.util.ListGenericToJsonConverter;

import java.util.List;

public class ListParameterDetailToJsonStringConverter extends ListGenericToJsonConverter<ParameterDetail> {
	public ListParameterDetailToJsonStringConverter() {
		super(new ObjectMapper(), new TypeReference<List<ParameterDetail>>() {
		});
	}
}
