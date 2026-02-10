package com.nextorm.portal.controller;

import com.nextorm.common.define.collector.CollectorTypeDefineRequestDto;
import com.nextorm.common.define.collector.DataCollectPlan;
import com.nextorm.portal.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {
	private final ConfigService configService;

	@GetMapping(path = "/collector")
	public List<DataCollectPlan> getByCollectorConfigName(@RequestParam("name") String configName) {
		return configService.getCollectorConfigByName(configName);
	}

	@PostMapping(path = "/collector/type-definition")
	public void saveCollectorTypeDefinition(@RequestBody CollectorTypeDefineRequestDto requestDto) {
		log.info("Collector Type Definition: {}", requestDto);
		configService.updateCollectorTypeDefinition(requestDto);
	}
}
