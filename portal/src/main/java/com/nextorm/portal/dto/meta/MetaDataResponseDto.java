package com.nextorm.portal.dto.meta;

import lombok.Getter;

import java.util.List;

@Getter
public class MetaDataResponseDto {
	@Getter
	public static class Item {
		private String id;
		private String name;

		public Item(
			String id,
			String name
		) {
			this.id = id;
			this.name = name;
		}
	}

	private List<Item> items;

	public static MetaDataResponseDto of(List<Item> items) {
		MetaDataResponseDto dto = new MetaDataResponseDto();
		dto.items = items;
		return dto;
	}
}
