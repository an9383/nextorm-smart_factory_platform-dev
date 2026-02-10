package com.nextorm.apcmodeling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.apcmodeling.dto.*;
import com.nextorm.common.apc.ApcModels;
import com.nextorm.common.apc.dto.ApcRequestSearchDto;
import com.nextorm.common.apc.dto.ApcRequestStatusDto;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.entity.ApcRequestResult;
import com.nextorm.common.apc.entity.BaseEntity;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import com.nextorm.common.apc.repository.ApcRequestResultRepository;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nextorm.common.apc.constant.ApcConstant.JOIN_DELIMITER;

@Service
@RequiredArgsConstructor
public class ApcResultService {

	private final ApcRequestRepository apcRequestRepository;
	private final ApcRequestResultRepository apcRequestResultRepository;
	private final CodeRepository codeRepository;

	public List<ApcRequestStatusResponseDto> getApcRequestStatusList(ApcResultSearchRequestDto searchRequestDto) {
		LocalDateTime from = searchRequestDto.getFrom();
		LocalDateTime to = searchRequestDto.getTo()
										   .withSecond(59);

		ApcRequest.Type type = Boolean.TRUE.equals(searchRequestDto.getIsIncludeSimulation())
							   ? null
							   : ApcRequest.Type.NORMAL;

		List<ApcRequestStatusDto> apcRequestStatusDatas = apcRequestRepository.findApcRequestStatusList(from, to, type);

		List<ApcRequestStatusDto> filteredApcRequestStatusDatas = apcRequestConditionFiltering(apcRequestStatusDatas,
			searchRequestDto.getCondition());

		return toApcResultListResponseDto(filteredApcRequestStatusDatas);
	}

	private List<ApcRequestStatusDto> apcRequestConditionFiltering(
		List<ApcRequestStatusDto> apcRequestStatusDatas,
		String requestCondition
	) {
		List<String> apcConditionCodes = codeRepository.findByCategory("APC_CONDITIONS")
													   .stream()
													   .map(Code::getCode)
													   .toList();
		return apcRequestStatusDatas.stream()
									.map(apcRequestStatusDto -> {
										Map<String, String> requestDataMap = jsonStringToMap(apcRequestStatusDto.getRequestDataJson());
										String joinCondition = conditionMapToStringWithDelimiter(apcConditionCodes,
											requestDataMap);
										return ApcModels.calculateSpecificityScore(joinCondition, requestCondition) >= 0
											   ? apcRequestStatusDto
											   : null;
									})
									.filter(Objects::nonNull)
									.toList();
	}

	private String conditionMapToStringWithDelimiter(
		List<String> codes,
		Map<String, String> conditionMap
	) {

		return codes.stream()
					.map(conditionMap::get)
					.collect(Collectors.joining(JOIN_DELIMITER));
	}

	private List<ApcRequestStatusResponseDto> toApcResultListResponseDto(List<ApcRequestStatusDto> filteredApcRequestStatusDatas) {
		return filteredApcRequestStatusDatas.stream()
											.map(apcRequestStatusDto -> {
												Map<String, String> requestMap = jsonStringToMap(apcRequestStatusDto.getRequestDataJson());
												return ApcRequestStatusResponseDto.from(apcRequestStatusDto,
													requestMap);
											})
											.toList();
	}

	public List<ApcRequestResultDto> getApcResultData(Long requestId) {
		List<ApcRequestResult> apcResults = apcRequestResultRepository.findByApcRequestId(requestId);
		return apcResults.stream()
						 .map(ApcRequestResultDto::from)
						 .toList();
	}

	public List<ApcResultTrendResponseDto> getResultParameterTrendData(
		ApcTrendSearchRequestDto apcTrendSearchRequestDto
	) {

		ApcRequestSearchDto apcRequestSearchDto = toApcRequestSearchDto(apcTrendSearchRequestDto);

		List<ApcRequest> apcRequests = apcRequestRepository.findBySearchParam(apcRequestSearchDto);
		List<Long> requestIds = apcRequests.stream()
										   .map(BaseEntity::getId)
										   .toList();
		List<ApcRequestResult> requestResults = apcRequestResultRepository.findAllByApcRequestIdIn(requestIds);

		return apcRequests.stream()
						  .map(apcRequest -> {
							  Map<String, String> resultMap = requestResultsToMap(requestResults, apcRequest.getId());
							  Map<String, String> requestMap = jsonStringToMap(apcRequest.getRequestDataJson());
							  return ApcResultTrendResponseDto.from(requestMap, apcRequest.getCreateAt(), resultMap);
						  })
						  .toList();
	}

	private ApcRequestSearchDto toApcRequestSearchDto(ApcTrendSearchRequestDto apcTrendSearchRequestDto) {

		ApcRequest.Status status = ApcRequest.Status.SUCCESS;
		ApcRequest.Type type = apcTrendSearchRequestDto.isIncludeSimulation()
							   ? null
							   : ApcRequest.Type.NORMAL;

		return ApcRequestSearchDto.builder()
								  .versionId(apcTrendSearchRequestDto.getVersionId())
								  .from(apcTrendSearchRequestDto.getFrom())
								  .to(apcTrendSearchRequestDto.getTo())
								  .status(status)
								  .type(type)
								  .build();
	}

	private Map<String, String> jsonStringToMap(String dataJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			if (dataJson == null) {
				return new HashMap<>();
			}
			return objectMapper.readValue(dataJson, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			return new HashMap<>();
		}
	}

	private Map<String, String> requestResultsToMap(
		List<ApcRequestResult> requestResults,
		Long apcRequestId
	) {
		return requestResults.stream()
							 .filter(apcRequestResult -> apcRequestResult.getApcRequestId()
																		 .equals(apcRequestId))
							 .collect(Collectors.toMap(ApcRequestResult::getResultKey,
								 ApcRequestResult::getResultValue));
	}
}
