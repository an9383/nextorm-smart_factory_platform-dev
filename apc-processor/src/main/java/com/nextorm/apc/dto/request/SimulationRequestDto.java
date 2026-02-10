package com.nextorm.apc.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class SimulationRequestDto {
	private Map<String, Object> source = Map.of();
}
