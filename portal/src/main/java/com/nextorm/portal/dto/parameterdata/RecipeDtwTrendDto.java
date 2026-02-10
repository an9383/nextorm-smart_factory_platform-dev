package com.nextorm.portal.dto.parameterdata;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecipeDtwTrendDto {
	private LocalDateTime start;
	private LocalDateTime end;
	private List<Double> values;

	public static RecipeDtwTrendDto of(
		LocalDateTime start,
		LocalDateTime end,
		List<Double> values
	) {
		RecipeDtwTrendDto dto = new RecipeDtwTrendDto();
		dto.setStart(start);
		dto.setEnd(end);
		dto.setValues(values);
		return dto;
	}
}
