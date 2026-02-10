package com.nextorm.portal.controller.widget;

import com.nextorm.portal.dto.widget.CollectStatusWidgetResponseDto;
import com.nextorm.portal.dto.widget.ToolStatusWidgetResponseDto;
import com.nextorm.portal.service.widget.CollectStatusWidgetService;
import com.nextorm.portal.service.widget.ToolStatusWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/widget-mgt")
@RequiredArgsConstructor
public class WidgetController {
	private final ToolStatusWidgetService toolStatusWidgetService;
	private final CollectStatusWidgetService collectStatusWidgetService;

	@GetMapping(value = "/tool-status-data/{toolId}")
	public ResponseEntity<ToolStatusWidgetResponseDto> getToolStatusWidgetData(@PathVariable(name = "toolId") Long toolId) {
		return ResponseEntity.ok()
							 .body(toolStatusWidgetService.getToolStatusWidgetData(toolId));
	}

	@GetMapping(value = "/collect-status-data/{toolId}")
	public ResponseEntity<CollectStatusWidgetResponseDto> getCollectStatusWidgetData(
		@PathVariable(name = "toolId") Long toolId
	) {
		return ResponseEntity.ok()
							 .body(collectStatusWidgetService.getCollectStatusWidgetData(toolId));
	}
}
