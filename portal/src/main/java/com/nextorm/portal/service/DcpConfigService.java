package com.nextorm.portal.service;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.repository.*;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.dcpconfig.DcpConfigNotFoundException;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigCreateRequestDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigResponseDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigSearchRequestDto;
import com.nextorm.portal.dto.dcpconfig.DcpConfigUpdateRequestDto;
import com.nextorm.portal.entity.system.RefreshEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DcpConfigService {
	private final ApplicationEventPublisher eventPublisher;
	private final DcpConfigRepository dcpConfigRepository;
	private final ParameterRepository parameterRepository;
	private final RuleRepository ruleRepository;
	private final ToolRepository toolRepository;
	private final ParameterExtraDataRepository parameterExtraDataRepository;

	public List<DcpConfigResponseDto> getDcpConfigs(DcpConfigSearchRequestDto searchRequestDto) {
		List<DcpConfig> result;

		// 검색 조건이 늘어나면 queryDsl로 변경해서 동적 쿼리에 대응하게 해야합니다.
		if (searchRequestDto.getToolId() != null) {
			result = dcpConfigRepository.findByToolIdWithParameters(searchRequestDto.getToolId());
		} else {
			result = dcpConfigRepository.findAllWithParameters();
		}

		return result.stream()
					 .map(DcpConfigResponseDto::from)
					 .toList();
	}

	public DcpConfigResponseDto createDcpConfig(DcpConfigCreateRequestDto dcpConfigDto) {
		Tool tool = toolRepository.findById(dcpConfigDto.getToolId())
								  .orElseThrow(RelateToolNotFoundException::new);
		List<Parameter> parameters = parameterRepository.findAllById(dcpConfigDto.getParameterIds());
		List<Rule> rules = ruleRepository.findAllById(dcpConfigDto.getRuleIds());

		DcpConfig dcpConfig = DcpConfig.create(dcpConfigDto.toEntity(), tool, parameters, rules);
		DcpConfigResponseDto dcpConfigResponseDto = DcpConfigResponseDto.from(dcpConfigRepository.save(dcpConfig));

		saveAndUpdateParameterExtraData(dcpConfigDto.getParameterCollectExtraData());

		eventPublisher.publishEvent(new RefreshEvent(dcpConfig.getTool()
															  .getId(), "CREATE_DCPCONFIG"));
		return dcpConfigResponseDto;
	}

	/**
	 * 파라미터 추가 데이터를 저장하고 업데이트합니다.
	 *
	 * @param parameterExtraDataMap 파라미터 추가 데이터 목록<br/>
	 *                              key: 파라미터 ID<br/>
	 *                              value: 추가 데이터
	 */
	private void saveAndUpdateParameterExtraData(Map<Long, Map<String, Object>> parameterExtraDataMap) {
		List<Long> extraDataParameterIds = parameterExtraDataMap.keySet()
																.stream()
																.toList();
		Map<Long, ParameterExtraData> foundExtraDataMap = findParameterExtraData(extraDataParameterIds);

		for (Long parameterId : extraDataParameterIds) {
			Map<String, Object> requestExtraData = parameterExtraDataMap.get(parameterId);

			ParameterExtraData foundExtraData = foundExtraDataMap.get(parameterId);
			if (foundExtraData != null) {
				foundExtraData.mergeExtraData(requestExtraData);
			} else {
				parameterExtraDataRepository.save(ParameterExtraData.create(parameterId, requestExtraData));
			}
		}
	}

	/**
	 * 파라미터 추가 데이터를 조회합니다.
	 */
	private Map<Long, ParameterExtraData> findParameterExtraData(List<Long> parameterIds) {
		return parameterExtraDataRepository.findByParameterIdIn(parameterIds)
										   .stream()
										   .collect(Collectors.toMap(ParameterExtraData::getParameterId, it -> it));

	}

	public DcpConfigResponseDto modifyDcpConfig(
		Long id,
		DcpConfigUpdateRequestDto dcpConfigUpdateRequestDto
	) {
		DcpConfig dcpConfig = dcpConfigRepository.findById(id)
												 .orElseThrow(DcpConfigNotFoundException::new);
		List<Parameter> parameters = parameterRepository.findAllById(dcpConfigUpdateRequestDto.getParameterIds());
		List<Rule> rules = ruleRepository.findAllById(dcpConfigUpdateRequestDto.getRuleIds());

		dcpConfig.modify(dcpConfigUpdateRequestDto.toEntity(), parameters, rules);
		saveAndUpdateParameterExtraData(dcpConfigUpdateRequestDto.getParameterCollectExtraData());

		eventPublisher.publishEvent(new RefreshEvent(dcpConfig.getTool()
															  .getId(), "MODIFY_DCPCONFIG"));
		return DcpConfigResponseDto.from(dcpConfig);
	}

	public void deleteDcpConfig(Long id) {
		DcpConfig dcpConfig = dcpConfigRepository.findById(id)
												 .orElseThrow(DcpConfigNotFoundException::new);
		List<Parameter> parameters = dcpConfig.getParameters()
											  .stream()
											  .filter(Parameter::isVirtual)
											  .toList();
		List<ConstraintViloationDto> exists = new ArrayList<>();
		if (!parameters.isEmpty()) {
			exists.addAll(parameters.stream()
									.map(parameter -> new ConstraintViloationDto("Modeling > VirtualParameter",
										parameter.getId(),
										parameter.getName(),
										null,
										null))
									.toList());
		}
		if (!exists.isEmpty()) {
			throw new ConstraintViloationException(exists);
		}

		dcpConfigRepository.deleteById(id);
		eventPublisher.publishEvent(new RefreshEvent(dcpConfig.getTool()
															  .getId(), "DELETE_DCPCONFIG"));
	}

	public DcpConfigResponseDto getDcpConfigById(Long id) {
		return DcpConfigResponseDto.from(dcpConfigRepository.findById(id)
															.orElse(null));
	}
}
