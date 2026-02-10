package com.nextorm.apcmodeling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApcTargetDataDto {
	private LocalDateTime traceAt;
	private Map<String, Object> data = new HashMap<>();
}
