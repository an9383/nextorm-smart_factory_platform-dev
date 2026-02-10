package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.sfp.SfpParameterResponseDto;
import com.nextorm.extensions.misillan.alarm.repository.SfpParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SfpParameterService {

	private final SfpParameterRepository sfpParameterRepository;


	public List<SfpParameterResponseDto> getAll() {
		return sfpParameterRepository.findAll()
									 .stream()
									 .map(SfpParameterResponseDto::from)
									 .collect(Collectors.toList());
	}
}
