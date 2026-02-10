package com.nextorm.portal.dto.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class I18nModifyRequestDto {
	private List<Item> newMessages = new ArrayList<>();
	private List<Item> updateMessages = new ArrayList<>();
	private List<String> removeKeys = new ArrayList<>();

	@Data
	public static class Item {
		private String key;
		private String lang;
		private String message;
	}
}
