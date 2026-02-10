package com.nextorm.common.db.entity;

import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class UiLanguageId implements Serializable {
	private String key;
	private String lang;

	@Builder
	private UiLanguageId(
		String key,
		String lang
	) {
		this.key = key;
		this.lang = lang;
	}

	public static UiLanguageId of(
		String key,
		String lang
	) {
		return UiLanguageId.builder()
						   .key(key)
						   .lang(lang)
						   .build();
	}
}
