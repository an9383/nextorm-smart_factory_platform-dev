package com.nextorm.extensions.koreawaida.roboshot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorDataRequestDto {
	private int machineId;
	private String exePath;
}
