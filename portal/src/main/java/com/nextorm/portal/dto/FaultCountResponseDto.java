package com.nextorm.portal.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class FaultCountResponseDto {
	private List<Item> items = new ArrayList<>();

	public FaultCountResponseDto(List<Item> items) {
		this.items = items;
	}

	@Getter
	public static class Item {
		Long parameterId;
		String parameterName;
		int count;

		public static Item of(
			Long parameterId,
			String parameterName,
			int count
		) {
			Item item = new Item();
			item.parameterId = parameterId;
			item.parameterName = parameterName;
			item.count = count;
			return item;
		}
	}
}
