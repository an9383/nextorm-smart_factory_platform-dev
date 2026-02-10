package com.nextorm.apc.controller;

import com.nextorm.apc.dto.request.ProcessRequestDto;
import com.nextorm.apc.dto.response.ProcessResponseDto;
import com.nextorm.apc.facade.ProcessFacade;
import com.nextorm.apc.service.NotifyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 외부 업체의 요청을 받기 위한 컨트롤러
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/process")
public class ProcessController {
	private final ProcessFacade processFacade;
	private final NotifyService notifyService;

	@PostMapping()
	public ProcessResponseDto process(@RequestBody ProcessRequestDto requestDto) {
		ProcessResponseDto responseDto = processFacade.process(requestDto.getSource());
		notifyService.asyncNotify(responseDto.getApcRequestId());
		return responseDto;
	}

}
