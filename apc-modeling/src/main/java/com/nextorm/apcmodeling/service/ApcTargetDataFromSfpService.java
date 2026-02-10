package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.dto.ApcTargetDataDto;
import com.nextorm.apcmodeling.service.interfaces.ApcTargetDataService;
import com.nextorm.common.apc.constant.ApcConstant;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("apcTargetDataFromSfpService")
@RequiredArgsConstructor
public class ApcTargetDataFromSfpService implements ApcTargetDataService {
	private final CodeRepository codeRepository;
	private final ParameterDataRepository parameterDataRepository;

	@Override
	public List<ApcTargetDataDto> getApcTargetData(
		LocalDateTime from,
		LocalDateTime to
	) {
		List<Code> dataParamCodes = codeRepository.findByCategory(ApcConstant.SFP_DATA_CATEGORY_CODE);
		List<Long> paramIds = dataParamCodes.stream()
											.map(code -> Long.parseLong(code.getValue()))
											.toList();
		List<TraceAtGroupParameterData> groupedParameterData = getParameterDataGroupedByTraceAt(paramIds, from, to);

		return groupedParameterData.stream()
								   .map(it -> ApcTargetDataDto.builder()
															  .traceAt(it.traceAt())
															  .data(parameterDataToMap(dataParamCodes,
																  it.parameterDataList()))
															  .build())
								   .toList();
	}

	private Map<String, Object> parameterDataToMap(
		List<Code> codes,
		List<ParameterData> parameterDataList
	) {
		Map<Long, String> codeValueMap = codes.stream()
											  .collect(Collectors.toMap(dto -> Long.parseLong(dto.getValue()),
												  Code::getCode));
		return parameterDataList.stream()
								.collect(Collectors.toMap(parameterData -> codeValueMap.get(parameterData.getParameterId()),
									ParameterData::getDValue));

	}

	public List<TraceAtGroupParameterData> getParameterDataGroupedByTraceAt(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(parameterIds,
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		Map<LocalDateTime, List<ParameterData>> groupedParameterDataMap = parameterDatas.stream()
																						.collect(Collectors.groupingBy(
																							it -> it.getTraceAt()
																									.withNano(0)));

		return groupedParameterDataMap.entrySet()
									  .stream()
									  .sorted(Map.Entry.comparingByKey())
									  .map(it -> new TraceAtGroupParameterData(it.getKey(), it.getValue()))
									  .toList();
	}

	private record TraceAtGroupParameterData(LocalDateTime traceAt, List<ParameterData> parameterDataList) {
	}

}
