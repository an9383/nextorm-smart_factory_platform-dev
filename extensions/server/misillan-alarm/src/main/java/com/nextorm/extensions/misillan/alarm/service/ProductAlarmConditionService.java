package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionCreateDto;
import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionModifyDto;
import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionResponseDto;
import com.nextorm.extensions.misillan.alarm.entity.EqmsProduct;
import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import com.nextorm.extensions.misillan.alarm.exception.eqms.EqmsProductNotFoundException;
import com.nextorm.extensions.misillan.alarm.exception.productalarmcondition.ProductAlarmConditionNotFoundException;
import com.nextorm.extensions.misillan.alarm.repository.EqmsProductRepository;
import com.nextorm.extensions.misillan.alarm.repository.ProductAlarmConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAlarmConditionService {
	private final EqmsProductRepository eqmsProductRepository;
	private final ProductAlarmConditionRepository productAlarmConditionRepository;

	@Transactional(readOnly = true)
	public List<ProductAlarmConditionResponseDto> getAll() {
		return productAlarmConditionRepository.findAll()
											  .stream()
											  .map(ProductAlarmConditionResponseDto::from)
											  .collect(Collectors.toList());
	}

	@Transactional
	public void createCondition(ProductAlarmConditionCreateDto conditionCreateDto) {
		EqmsProduct eqmsProduct = eqmsProductRepository.findById(conditionCreateDto.getProductId())
													   .orElseThrow(EqmsProductNotFoundException::new);

		productAlarmConditionRepository.save(ProductAlarmConditionCreateDto.toEntity(eqmsProduct, conditionCreateDto));
	}

	@Transactional
	public void modifyCondition(
		Long conditionId,
		ProductAlarmConditionModifyDto conditionModifyDto
	) {
		EqmsProduct eqmsProduct = eqmsProductRepository.findById(conditionModifyDto.getProductId())
													   .orElseThrow(EqmsProductNotFoundException::new);

		ProductAlarmCondition productAlarmCondition = productAlarmConditionRepository.findById(conditionId)
																					 .orElseThrow(
																						 ProductAlarmConditionNotFoundException::new);
		productAlarmCondition.modify(eqmsProduct, conditionModifyDto);
	}

	@Transactional
	public void deleteCondition(Long conditionId) {
		ProductAlarmCondition productAlarmCondition = productAlarmConditionRepository.findById(conditionId)
																					 .orElseThrow(
																						 ProductAlarmConditionNotFoundException::new);
		productAlarmConditionRepository.delete(productAlarmCondition);
	}
}
