package com.nextorm.portal.controller;

import com.nextorm.portal.dto.ocap.*;
import com.nextorm.portal.service.OcapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/ocap")
@RestController
@RequiredArgsConstructor
public class OcapController {

	private final OcapService ocapService;

	@GetMapping
	public List<OcapResponseDto> getAll() {
		return ocapService.getAll();
	}

	@GetMapping("/{id}")
	public OcapResponseDto get(@PathVariable(name = "id") Long id) {
		return ocapService.getById(id);
	}

	@GetMapping("/histories")
	public List<OcapAlarmHistoryResponseDto> getHistories(OcapAlarmHistorySearchRequestDto searchParam) {
		return ocapService.getHistories(searchParam);
	}

	@PostMapping
	public void create(@RequestBody OcapCreateRequestDto requestDto) {
		log.info("create: {}", requestDto);
		ocapService.create(requestDto);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable(name = "id") Long id) {
		ocapService.delete(id);
	}

	@PutMapping("/{id}")
	public void modify(
		@PathVariable(name = "id") Long id,
		@RequestBody OcapModifyRequestDto requestDto
	) {
		ocapService.modify(id, requestDto);
	}

}
