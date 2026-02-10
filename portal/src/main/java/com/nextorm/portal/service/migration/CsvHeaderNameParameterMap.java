package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.Parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CsvHeaderNameParameterMap {
	private final Map<String, Parameter> headerToParameterMap = new HashMap<>();

	public void addMapping(
		String header,
		Parameter parameter
	) {
		headerToParameterMap.put(header, parameter);
	}

	public Parameter getParameter(String header) {
		return headerToParameterMap.get(header);
	}

	public boolean containsHeader(String header) {
		return headerToParameterMap.containsKey(header);
	}

	public Set<String> getHeaders() {
		return headerToParameterMap.keySet();
	}
}
