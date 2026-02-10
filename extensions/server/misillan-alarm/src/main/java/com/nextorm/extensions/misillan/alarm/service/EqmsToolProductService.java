package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.eqms.EqmsProductResponseDto;
import com.nextorm.extensions.misillan.alarm.dto.eqms.EqmsToolResponseDto;
import com.nextorm.extensions.misillan.alarm.repository.EqmsProductRepository;
import com.nextorm.extensions.misillan.alarm.repository.EqmsToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EqmsToolProductService {
	private final EqmsToolRepository eqmsToolRepository;
	private final EqmsProductRepository eqmsProductRepository;

	public List<EqmsToolResponseDto> getToolList() {
		return eqmsToolRepository.findAll()
								 .stream()
								 .map(EqmsToolResponseDto::from)
								 .collect(Collectors.toList());
	}

	public List<EqmsProductResponseDto> getParameterList() {
		return eqmsProductRepository.findAll()
								 .stream()
								 .map(EqmsProductResponseDto::from)
								 .collect(Collectors.toList());
	}


}
