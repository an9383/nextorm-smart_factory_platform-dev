package com.nextorm.portal.service;

import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Rule;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.RuleRepository;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.rule.RuleNameDuplicationException;
import com.nextorm.portal.common.exception.rule.RuleNotFoundException;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.rule.RuleCreateRequestDto;
import com.nextorm.portal.dto.rule.RuleResponseDto;
import com.nextorm.portal.dto.rule.RuleUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RuleService {
	private final RuleRepository ruleRepository;
	private final DcpConfigRepository dcpConfigRepository;

	public List<RuleResponseDto> getRules() {
		return ruleRepository.findAll()
							 .stream()
							 .map(RuleResponseDto::from)
							 .toList();
	}

	public RuleResponseDto createRule(RuleCreateRequestDto ruleCreateRequestDto) {
		Optional<Rule> optionalRule = ruleRepository.findByName(ruleCreateRequestDto.getName());
		if (optionalRule.isPresent()) {
			throw new RuleNameDuplicationException(ruleCreateRequestDto.getName());
		}
		Rule rule = ruleCreateRequestDto.toEntity();
		return RuleResponseDto.from(ruleRepository.save(rule));
	}

	public RuleResponseDto modifyRule(
		Long ruleId,
		RuleUpdateRequestDto ruleUpdateRequestDto
	) {
		Rule rule = ruleRepository.findById(ruleId)
								  .orElseThrow(RuleNotFoundException::new);
		Optional<Rule> optionalRule = ruleRepository.findByName(ruleUpdateRequestDto.getName());
		if (!rule.getName()
				 .equals(ruleUpdateRequestDto.getName()) && optionalRule.isPresent()) {
			throw new RuleNameDuplicationException(ruleUpdateRequestDto.getName());
		}

		rule.modify(ruleUpdateRequestDto.toEntity());
		return RuleResponseDto.from(rule);

	}

	public void deleteRule(Long id) {
		Rule rule = ruleRepository.findById(id)
								  .orElseThrow(RuleNotFoundException::new);
		List<DcpConfig> dcpConfigs = dcpConfigRepository.findAllByRuleId(rule.getId());
		List<ConstraintViloationDto> exists = new ArrayList<>();

		if (!dcpConfigs.isEmpty()) {
			exists.addAll(dcpConfigs.stream()
									.map(dcpConfig -> new ConstraintViloationDto("Modeling > Dcp Config",
										dcpConfig.getId(),
										dcpConfig.getTopic(),
										null,
										null))
									.toList());
		}
		if (!exists.isEmpty()) {
			throw new ConstraintViloationException(exists);
		}

		ruleRepository.deleteById(id);
	}
}
