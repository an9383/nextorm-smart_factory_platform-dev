package com.nextorm.collector.service;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.repository.ParameterExtraDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParameterQueryService {
	private static final String EXTRA_DATA_NOT_FOUND_ERROR_MESSAGE = "툴 ID: %s의 파라미터 이름: %s의 추가 데이터를 찾을 수 없습니다.";
	private static final String PARAMETER_NOT_FOUND_ERROR_MESSAGE = "파라미터를 찾을 수 없습니다. (ID :%s)";

	private final ParameterExtraDataRepository parameterExtraDataRepository;
	private final ParameterRepository parameterRepository;

	public ParameterDetail getParameterDetail(
		Long toolId,
		String parameterName
	) {
		return parameterExtraDataRepository.findByToolIdAndParameterName(toolId, parameterName)
										   .map(it -> {
											   Parameter parameter = parameterRepository.findById(it.getParameterId())
																						.orElseThrow(() -> new IllegalArgumentException(
																							PARAMETER_NOT_FOUND_ERROR_MESSAGE.formatted(
																								it.getParameterId())));

											   return ParameterDetail.builder()
																	 .id(parameter.getId())
																	 .name(parameterName)
																	 .toolId(toolId)
																	 .dataType(parameter.getDataType())
																	 .extraData(it.getExtraData())
																	 .build();

										   })
										   .orElseThrow(() -> new IllegalArgumentException(
											   EXTRA_DATA_NOT_FOUND_ERROR_MESSAGE.formatted(toolId, parameterName)));

	}

}
