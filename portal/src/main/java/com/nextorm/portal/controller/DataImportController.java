package com.nextorm.portal.controller;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.portal.dto.parameter.ParameterResponseDto;
import com.nextorm.portal.dto.parameter.ParameterSearchRequestDto;
import com.nextorm.portal.dto.parameterdata.ParameterDataImportDto;
import com.nextorm.portal.service.DataImportService;
import com.nextorm.portal.service.parameter.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data-mgt")
@RequiredArgsConstructor
public class DataImportController {

	private final ParameterService parameterService;

	private final DataImportService dataImportService;

	@PostMapping(path = "/data")
	public ResponseEntity<Object> saveData(@RequestBody ParameterDataImportDto parameterDataImportDto) {
		ParameterSearchRequestDto parameterSearchRequestDto = new ParameterSearchRequestDto();
		parameterSearchRequestDto.setName(parameterDataImportDto.getParameterName());

		List<ParameterResponseDto> parameters = parameterService.getParameters(parameterSearchRequestDto);
		ParameterResponseDto parameter = parameters.isEmpty()
										 ? null
										 : parameters.get(0);

		if (parameter == null) {
			return ResponseEntity.ofNullable("Parameter not exist!" + parameterDataImportDto.getParameterName());
		}
		List<ParameterData> parameterDataList = null;
		if (parameter.getDataType() == Parameter.DataType.DOUBLE) {
			List<Object> datas = parameterDataImportDto.getDatas();
			parameterDataList = datas.stream()
									 .map(v -> {
										 return ParameterData.builder()
															 .parameterId(parameter.getId())
															 .dValue(Double.valueOf(((ArrayList<?>)v).get(1)
																									 .toString()))
															 .traceAt(LocalDateTime.ofInstant(Instant.parse(((ArrayList<?>)v).get(
																																 0)
																															 .toString()),
																 ZoneId.systemDefault()))
															 .build();
									 })
									 .collect(Collectors.toList());
		} else if (parameter.getDataType() == Parameter.DataType.INTEGER) {
			List<Object> datas = parameterDataImportDto.getDatas();
			parameterDataList = datas.stream()
									 .map(v -> {
										 return ParameterData.builder()
															 .parameterId(parameter.getId())
															 .iValue((Integer)((ArrayList<?>)v).get(1))
															 .traceAt((LocalDateTime)((ArrayList<?>)v).get(0))
															 .build();
									 })
									 .collect(Collectors.toList());
		} else if (parameter.getDataType() == Parameter.DataType.STRING) {
			List<Object> datas = parameterDataImportDto.getDatas();
			parameterDataList = datas.stream()
									 .map(v -> {
										 return ParameterData.builder()
															 .parameterId(parameter.getId())
															 .sValue((String)((ArrayList<?>)v).get(1))
															 .traceAt((LocalDateTime)((ArrayList<?>)v).get(0))
															 .build();
									 })
									 .collect(Collectors.toList());
		}

		dataImportService.saveBulk(parameterDataList);

		return ResponseEntity.ok("success");
	}

}
