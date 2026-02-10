package com.nextorm.portal.service.widget;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.dto.widget.CollectStatusWidgetResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectStatusWidgetService {
	private CollectStatusWidgetResponseDto collectStatusWidgetResponseDto;
	private final ToolRepository toolRepository;
	private final ParameterRepository parameterRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final ParameterDataRepository parameterDataRepository;

	public CollectStatusWidgetResponseDto getCollectStatusWidgetData(Long toolId) {
		String parameterGoodList = "parameterGoodList";
		String parameterBadList = "parameterBadList";
		String dcpGoodCnt = "dcpGoodCnt";
		String dcpBadCnt = "dcpBadCnt";
		CollectStatusWidgetResponseDto collectStatusWidgetResponseDto = new CollectStatusWidgetResponseDto();
		Tool tool = toolRepository.findById(toolId)
								  .orElseThrow(RelateToolNotFoundException::new);
		List<DcpConfig> dcpConfigs = dcpConfigRepository.findByToolId(toolId);

		Map<String, List<Parameter>> parameterCollectMap = getParameterCollectStatus(toolId,
			parameterGoodList,
			parameterBadList);

		Map<String, Integer> dcpCollectCntMap = getDcpCollectStatus(dcpConfigs, dcpGoodCnt, dcpBadCnt);
		collectStatusWidgetResponseDto = collectStatusWidgetResponseDto.builder()
																	   .toolId(tool.getId())
																	   .toolName(tool.getName())
																	   .dcpIds(dcpConfigs.stream()
																						 .map(DcpConfig::getId)
																						 .toList())
																	   .dcpGoodCnt(dcpCollectCntMap.get(dcpGoodCnt))
																	   .dcpBadCnt(dcpCollectCntMap.get(dcpBadCnt))
																	   .parameterGoodCnt(parameterCollectMap.get(
																												parameterGoodList)
																											.size())
																	   .parameterBadCnt(parameterCollectMap.get(
																											   parameterBadList)
																										   .size())
																	   .lastCollectedAtList(dcpConfigs.stream()
																									  .map(DcpConfig::getLastCollectedAt)
																									  .toList())
																	   .goodCollectedParameterIds(parameterCollectMap.get(
																														 parameterGoodList)
																													 .stream()
																													 .map(
																														 Parameter::getId)
																													 .toList())
																	   .badCollectedParameterIds(parameterCollectMap.get(
																														parameterBadList)
																													.stream()
																													.map(
																														Parameter::getId)
																													.toList())
																	   .goodCollectedParameterNames(parameterCollectMap.get(
																														   parameterGoodList)
																													   .stream()
																													   .map(
																														   Parameter::getName)
																													   .toList())
																	   .badCollectedParameterNames(parameterCollectMap.get(
																														  parameterBadList)
																													  .stream()
																													  .map(
																														  Parameter::getName)
																													  .toList())
																	   .build();
		return collectStatusWidgetResponseDto;
	}

	private Map<String, Integer> getDcpCollectStatus(
		List<DcpConfig> dcpConfigs,
		String dcpGoodCnt,
		String dcpBadCnt
	) {
		int dcpGoodCount = 0;
		int dcpBadCount = 0;
		LocalDateTime currentDateTime = LocalDateTime.now();
		Map<String, Integer> dcpCollectCntMap = new HashMap<>();
		for (DcpConfig dcpConfig : dcpConfigs) {
			if (dcpConfig != null && dcpConfig.getLastCollectedAt() != null && currentDateTime.minusSeconds(dcpConfig.getDataInterval() * 3L)
																							  .isBefore(dcpConfig.getLastCollectedAt())) {
				dcpGoodCount++;
			} else {
				dcpBadCount++;
			}
		}
		dcpCollectCntMap.put(dcpGoodCnt, dcpGoodCount);
		dcpCollectCntMap.put(dcpBadCnt, dcpBadCount);
		return dcpCollectCntMap;
	}

	private Map<String, List<Parameter>> getParameterCollectStatus(
		Long toolId,
		String parameterGood,
		String parameterBad
	) {
		List<Parameter> goodCollectedParameters = new ArrayList<>();
		List<Parameter> badCollectedParameters = new ArrayList<>();
		Map<String, List<Parameter>> parameterCollectMap = new HashMap<>();

		List<Parameter> parameters = parameterRepository.findByToolId(toolId);
		List<Long> parameterIds = parameters.stream()
											.map(BaseEntity::getId)
											.toList();

		List<DcpConfig> dcpConfigs = dcpConfigRepository.findByToolId(toolId);
		LocalDateTime oneHourAgo = LocalDateTime.now()
												.minusHours(1);
		LocalDateTime currentDateTime = LocalDateTime.now();

		List<ParameterData> latestParameterData = parameterDataRepository.findLatestParameterData(parameterIds,
			oneHourAgo);

		for (Parameter param : parameters) {
			Optional<ParameterData> latestDataOpt = latestParameterData.stream()
																	   .filter(data -> data.getParameterId()
																						   .equals(param.getId()))
																	   .findFirst();

			if (latestDataOpt.isEmpty()) {
				badCollectedParameters.add(param);
				continue;
			}

			boolean isGood = false;
			ParameterData latestData = latestDataOpt.get();
			DcpConfig dcpConfig = findDcpConfigForParameter(dcpConfigs, latestData.getParameterId());

			if (dcpConfig != null && isDataRecent(latestData, dcpConfig, currentDateTime)) {
				isGood = true;
			}

			if (isGood) {
				goodCollectedParameters.add(param);
			} else {
				badCollectedParameters.add(param);
			}
		}

		parameterCollectMap.put(parameterGood, goodCollectedParameters);
		parameterCollectMap.put(parameterBad, badCollectedParameters);
		return parameterCollectMap;
	}

	private Map<String, Parameter> mapParametersByName(List<Parameter> parameters) {
		Map<String, Parameter> parameterMap = new HashMap<>();
		for (Parameter parameter : parameters) {
			parameterMap.put(parameter.getName(), parameter);
		}
		return parameterMap;
	}

	//현재 데이터의 TraceAt 시간이 (현재시간 - Data Interval * 3) 보다 안에있는지 체크한다.
	private boolean isDataRecent(
		ParameterData data,
		DcpConfig dcpConfig,
		LocalDateTime currentDateTime
	) {
		return currentDateTime.minusSeconds(dcpConfig.getDataInterval() * 3L)
							  .isBefore(data.getTraceAt());
	}

	/*
		파라미터가 속한 Dcpconfig를 Return
	 */
	private DcpConfig findDcpConfigForParameter(
		List<DcpConfig> dcpConfigs,
		Long parameterId
	) {
		return dcpConfigs.stream()
						 .filter(d -> d.getParameters()
									   .stream()
									   .anyMatch(p -> p.getId()
													   .equals(parameterId)))
						 .findFirst()
						 .orElse(null);
	}
}
