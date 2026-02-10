package com.nextorm.portal.dto.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfoDto {
	private String systemKey;
	private String value;
	private String largeValue;

}