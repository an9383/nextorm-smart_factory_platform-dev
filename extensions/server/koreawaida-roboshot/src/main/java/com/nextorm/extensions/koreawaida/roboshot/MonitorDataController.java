package com.nextorm.extensions.koreawaida.roboshot;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorDataController {

	private final MonitorDataService monitorDataService;

	@PostMapping(path = "/data")
	public List<String> getCurrMonitorDataById(
		@RequestBody MonitorDataRequestDto monitorDataRequestDto
	) {
		int machineId = monitorDataRequestDto.getMachineId();
		String exePath = monitorDataRequestDto.getExePath();
		return monitorDataService.getCurrMonitorDataById(machineId, exePath);
	}
}
