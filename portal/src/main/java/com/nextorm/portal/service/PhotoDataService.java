package com.nextorm.portal.service;

import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.portal.dto.parameterdata.GPSCoordinateDto;
import com.nextorm.portal.dto.parameterdata.RobotPhotoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoDataService {
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterRepository parameterRepository;

	public List<RobotPhotoResponseDto> getRobotPhotoData(
		Long toolId,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		Parameter.DataType dataType = Parameter.DataType.IMAGE;
		List<ParameterData> parameterDatas = getParameterDatasByToolIdAndDataType(toolId, dataType, fromDate, toDate);

		Map<GPSCoordinateDto, List<ParameterData>> gpsCoordinateMap = groupingGpsCoordinate(parameterDatas);
		return toRobotPhotoDatas(gpsCoordinateMap);

	}

	private List<ParameterData> getParameterDatasByToolIdAndDataType(
		Long toolId,
		Parameter.DataType dataType,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		List<Long> parameterIds = parameterRepository.findByToolIdAndDataType(toolId, dataType)
													 .stream()
													 .map(BaseEntity::getId)
													 .toList();

		return parameterDataRepository.findByParameterIdInAndTraceAtGreaterThanEqualAndTraceAtLessThan(parameterIds,
			fromDate,
			toDate,
			null);
	}

	private Map<GPSCoordinateDto, List<ParameterData>> groupingGpsCoordinate(List<ParameterData> parameterDatas) {
		return parameterDatas.stream()
							 .filter(parameterData -> parameterData.getImageValue() != null)
							 .collect(Collectors.groupingBy(parameterData -> {
								 long distance = 5000;
								 double lat = Math.floor(parameterData.getLatitudeValue() * distance) / distance;
								 double lng = Math.floor(parameterData.getLongitudeValue() * distance) / distance;
								 return new GPSCoordinateDto(lat, lng);
							 }));
	}

	private List<RobotPhotoResponseDto> toRobotPhotoDatas(Map<GPSCoordinateDto, List<ParameterData>> gpsCoordinateMap) {
		return gpsCoordinateMap.keySet()
							   .stream()
							   .map(key -> {
								   List<ParameterData> datas = gpsCoordinateMap.get(key);
								   ParameterData parameterData = datas.stream()
																	  .max(Comparator.comparing(ParameterData::getTraceAt))
																	  .orElseThrow();
								   String imageUrl = "/img/ext/photo/" + parameterData.getImageValue();

								   return RobotPhotoResponseDto.of(key, imageUrl, parameterData.getTraceAt());
							   })
							   .toList();
	}
}
