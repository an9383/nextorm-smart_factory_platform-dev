package com.nextorm.portal.dto.parameterdata;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class RecipeTrendDto {
	private LocalDateTime start;
	private LocalDateTime end;
	private List<Item> items;

	public static RecipeTrendDto of(
		LocalDateTime start,
		LocalDateTime end,
		List<Item> items
	) {
		RecipeTrendDto dto = new RecipeTrendDto();
		dto.setStart(start);
		dto.setEnd(end);
		dto.setItems(items);
		return dto;
	}

	@Getter
	public static class Item {
		private LocalTime time;
		private Number value;

		public static Item of(
			LocalTime time,
			Number value
		) {
			Item item = new Item();
			item.time = time;
			item.value = value;
			return item;
		}
	}

}
