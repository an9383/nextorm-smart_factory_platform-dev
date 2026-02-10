package com.nextorm.portal.controller;

import com.nextorm.portal.dto.dcpconfig.DcpConfigCreateRequestDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigResponseDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigSearchRequestDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigUpdateRequestDto;
import com.nextorm.portal.service.DcpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dcp-configs")
@RequiredArgsConstructor
public class DcpConfigController {
	private final DcpConfigService dcpConfigService;

	@GetMapping(path = "")
	public List<DcpConfigResponseDto> getDcpConfigs(DcpConfigSearchRequestDto searchRequestDto) {
		return dcpConfigService.getDcpConfigs(searchRequestDto);
	}

	@GetMapping(path = "/{id}")
	public DcpConfigResponseDto getDcpConfigById(@PathVariable(name = "id") Long id) {
		DcpConfigResponseDto dcpConfigResponseDto = dcpConfigService.getDcpConfigById(id);
		return dcpConfigResponseDto;
	}

	@PostMapping(path = "")
	public ResponseEntity<DcpConfigResponseDto> createDcpConfig(@RequestBody DcpConfigCreateRequestDto dcpConfigCreateRequestDto) {
		DcpConfigResponseDto dcpConfigResponseDto = dcpConfigService.createDcpConfig(dcpConfigCreateRequestDto);
		return ResponseEntity.ok(dcpConfigResponseDto);
	}

	@PutMapping(path = "/{id}")
	public ResponseEntity<DcpConfigResponseDto> modifyDcpConfig(
		@PathVariable(name = "id") Long id,
		@RequestBody DcpConfigUpdateRequestDto dcpConfigUpdateRequestDto
	) {
		DcpConfigResponseDto result = dcpConfigService.modifyDcpConfig(id, dcpConfigUpdateRequestDto);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<String> deleteDcpConfig(@PathVariable(name = "id") Long id) {
		dcpConfigService.deleteDcpConfig(id);
		return ResponseEntity.ok("ok");
	}
}
