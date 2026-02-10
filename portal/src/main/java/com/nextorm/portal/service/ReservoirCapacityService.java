package com.nextorm.portal.service;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.entity.ReservoirCapacity;
import com.nextorm.common.db.repository.LocationRepository;
import com.nextorm.common.db.repository.ReservoirCapacityRepository;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.location.RelateLocationNotFoundException;
import com.nextorm.portal.common.exception.reservoircapacity.ReservoirCapacityDuplicationException;
import com.nextorm.portal.common.exception.reservoircapacity.ReservoirCapacityNotFoundException;
import com.nextorm.portal.controller.ReservoirCapacityController;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.reservoircapacity.*;
import com.nextorm.portal.dto.weather.PastWeatherResponseDto;
import com.nextorm.portal.dto.weather.ShortForcecastWeatherResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReservoirCapacityService {
	private final ReservoirCapacityRepository reservoirCapacityRepository;
	private final LocationRepository locationRepository;
	private final WeatherService weatherService;

	private static final String DATE_FORMAT = "yyyyMMdd";
	private static final String TIME_FORMAT = "HHmm";

	public List<ReservoirCapacityResponseDto> getReservoirCapacities(
		ReservoirCapacitySearchDto reservoirCapacitySearch
	) {
		LocalDateTime startDateTime = reservoirCapacitySearch.getStartDt()
															 .toLocalDate()
															 .atStartOfDay();
		LocalDateTime endDateTime = reservoirCapacitySearch.getEndDt()
														   .toLocalDate()
														   .atTime(23, 59, 59, 999);

		return reservoirCapacityRepository.findByPeriod(startDateTime, endDateTime)
										  .stream()
										  .map(ReservoirCapacityResponseDto::from)
										  .collect(Collectors.toList());
	}

	public ReservoirCapacityResponseDto getReservoirCapacity(Long id) {
		ReservoirCapacity reservoirCapacity = reservoirCapacityRepository.findByReservoirCapacityId(id);
		if (reservoirCapacity == null) {
			throw new ReservoirCapacityNotFoundException();
		}
		return ReservoirCapacityResponseDto.from(reservoirCapacity);
	}

	public ReservoirCapacityResponseDto createReservoirCapacity(ReservoirCapacityCreateRequestDto reservoirCapacityCreateRequestDto) {
		Location location = locationRepository.findById(reservoirCapacityCreateRequestDto.getLocationId())
											  .orElseThrow(RelateLocationNotFoundException::new);

		if (isExistReservoirCapacity(null,
			reservoirCapacityCreateRequestDto.getLocationId(),
			reservoirCapacityCreateRequestDto.getDate())) {
			throw new ReservoirCapacityDuplicationException(); //수정 이미 존재하는 날짜 Exsits
		}

		ReservoirCapacity reservoirCapacity = reservoirCapacityCreateRequestDto.toEntity(location);
		return ReservoirCapacityResponseDto.from(reservoirCapacityRepository.save(reservoirCapacity));
	}

	public ReservoirCapacityResponseDto modifyReservoirCapacity(
		Long reservoirCapacityId,
		ReservoirCapacityUpdateRequestDto reservoirCapacityUpdateRequestDto
	) {
		ReservoirCapacity reservoirCapacity = reservoirCapacityRepository.findById(reservoirCapacityId)
																		 .orElseThrow(ReservoirCapacityNotFoundException::new);

		if (isExistReservoirCapacity(reservoirCapacityId,
			reservoirCapacityUpdateRequestDto.getLocationId(),
			reservoirCapacityUpdateRequestDto.getDate())) {
			throw new ReservoirCapacityDuplicationException();
		}

		ReservoirCapacity reservoirCapacityEntity = reservoirCapacityUpdateRequestDto.toEntity(reservoirCapacity.getLocation());

		reservoirCapacity.modify(reservoirCapacityEntity.getReservoirCapacity(),
			reservoirCapacityEntity.getRainFall(),
			reservoirCapacityEntity.getDate(),
			reservoirCapacityEntity.getDescription());
		return ReservoirCapacityResponseDto.from(reservoirCapacity);
	}

	public void deleteReservoirCapacity(Long reservoirCapacityId) {
		reservoirCapacityRepository.deleteById(reservoirCapacityId);
	}

	public void deleteReservoirCapacities(List<Long> ids) throws ConstraintViloationException {
		List<ConstraintViloationDto> constraintDatas = new ArrayList<>();
		for (Long id : ids) {
			try {
				this.deleteReservoirCapacity(id);
			} catch (ConstraintViloationException e) {
				constraintDatas.addAll(e.getData());
			}
		}
		if (constraintDatas.size() > 0) {
			throw new ConstraintViloationException(constraintDatas);
		}
	}

	public boolean isExistReservoirCapacity(
		Long reservoirCapacityId,
		Long locationId,
		LocalDateTime date
	) {
		boolean result = false;
		ReservoirCapacity reservoirCapacity = reservoirCapacityRepository.findByLocationIdAndDate(locationId, date);
		if (reservoirCapacity != null && reservoirCapacityId != reservoirCapacity.getId()) {
			result = true;
		}
		return result;
	}

	public List<ReservoirCapacityResponseTrendDto> getReservoirCapacityTrend(
		ReservoirCapacityController.Type type,
		LocalDateTime startDt,
		LocalDateTime endDt,
		Long locationId
	) {
		String formattedEndDt = endDt.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String yesterDay = LocalDateTime.now()
										.minusDays(1)
										.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		List<PastWeatherResponseDto> shortForcecastWeatherRainfallList = new ArrayList<>();

		// 저수량 데이터 조회
		List<ReservoirCapacityResponseTrendDto> reservoirCapacityDatas = this.getReservoirCapacityData(type,
			startDt,
			endDt,
			locationId);

		if ((type == ReservoirCapacityController.Type.DAY || type == ReservoirCapacityController.Type.HOUR) && formattedEndDt.equals(
			yesterDay)) {
			//3일치 단기 강수량 예보 데이터 조회 (오늘,내일,모레)
			Location location = locationRepository.findById(locationId)
												  .orElseThrow(RelateLocationNotFoundException::new);

			Double latitude = location.getLatitude();
			Double longitude = location.getLongitude();
			if (latitude != null && longitude != null) {
				shortForcecastWeatherRainfallList = this.getShortForecastWeatherRainfall(latitude, longitude, type);
			}
		}

		// 강수량 데이터와 저수량 데이터 병합
		List<ReservoirCapacityResponseTrendDto> resultDatas = this.mergeReservoirCapacityRainfall(reservoirCapacityDatas,
			shortForcecastWeatherRainfallList);

		return resultDatas;
	}

	public List<ReservoirCapacityResponseTrendDto> getReservoirCapacityData(
		ReservoirCapacityController.Type type,
		LocalDateTime startDt,
		LocalDateTime endDt,
		Long locationId
	) {
		LocalDateTime startDateTime = startDt.toLocalDate()
											 .atStartOfDay();
		LocalDateTime endDateTime = endDt.toLocalDate()
										 .atTime(23, 59, 59, 999);
		List<ReservoirCapacity> reservoirCapacityList = reservoirCapacityRepository.findByPeriodAndLocation(
			startDateTime,
			endDateTime,
			locationId);

		String formattedEndDt = endDt.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		String yesterDay = LocalDateTime.now()
										.minusDays(1)
										.format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		//type별로 grouping = MONTH, DAY, HOUR
		Map<LocalDateTime, List<ReservoirCapacity>> groupedData = reservoirCapacityList.stream()
																					   .collect(Collectors.groupingBy(
																						   reservoirCapacity -> {
																							   LocalDateTime date = reservoirCapacity.getDate();
																							   switch (type) {
																								   case YEAR:
																									   return date.withDayOfYear(
																													  1)
																												  .truncatedTo(
																													  ChronoUnit.DAYS);
																								   case MONTH:
																									   return date.withDayOfMonth(
																													  1)
																												  .truncatedTo(
																													  ChronoUnit.DAYS);
																								   case DAY:
																									   return date.truncatedTo(
																										   ChronoUnit.DAYS);
																								   case HOUR:
																									   return date.truncatedTo(
																										   ChronoUnit.HOURS);
																								   default:
																									   throw new IllegalArgumentException(
																										   "Invalid type: " + type);
																							   }
																						   }));

		return groupedData.entrySet()
						  .stream()
						  .map(entry -> {
							  LocalDateTime date = entry.getKey();
							  Double averageCapacity = entry.getValue()
															.stream()
															.collect(Collectors.averagingDouble(ReservoirCapacity::getReservoirCapacity));
							  Double sumRainfall = entry.getValue()
														.stream()
														.collect(Collectors.summingDouble(ReservoirCapacity::getRainFall));
							  ReservoirCapacityResponseTrendDto dto = new ReservoirCapacityResponseTrendDto();
							  dto.setDate(date);
							  dto.setReservoirCapacity(averageCapacity);
							  dto.setRainFall(sumRainfall);
							  return dto;
						  })
						  .sorted(Comparator.comparing(ReservoirCapacityResponseTrendDto::getDate))
						  .collect(Collectors.toList());

	}

	private List<ReservoirCapacityResponseTrendDto> mergeReservoirCapacityRainfall(
		List<ReservoirCapacityResponseTrendDto> reservoirCapacityDatas,
		List<PastWeatherResponseDto> shortForcecastWeatherRainfallList
	) {
		Double lastReservoirCapacity = reservoirCapacityDatas.stream()
															 .map(ReservoirCapacityResponseTrendDto::getReservoirCapacity)
															 .filter(capacity -> capacity != null)
															 .reduce((first, second) -> second)
															 .orElse(0.0);

		//3일치 단기 강수량 예보 데이터가 있는 경우 업데이트
		for (PastWeatherResponseDto shortForcecastWeatherRainfall : shortForcecastWeatherRainfallList) {
			Double tempReservoirCapacity = this.updateCapacityByRainfallPercent(lastReservoirCapacity,
				shortForcecastWeatherRainfall.getSumRain());

			ReservoirCapacityResponseTrendDto reservoirCapacityDto = new ReservoirCapacityResponseTrendDto();
			reservoirCapacityDto.setReservoirCapacity(tempReservoirCapacity);
			reservoirCapacityDto.setDate(LocalDateTime.parse(shortForcecastWeatherRainfall.getDate()));
			reservoirCapacityDto.setRainFall((shortForcecastWeatherRainfall.getSumRain()));
			reservoirCapacityDto.setIsPredicted(true);
			reservoirCapacityDatas.add(reservoirCapacityDto);
			lastReservoirCapacity = tempReservoirCapacity;
		}

		return reservoirCapacityDatas.stream()
									 .sorted(Comparator.comparing(ReservoirCapacityResponseTrendDto::getDate))
									 .collect(Collectors.toList());

	}

	private double updateCapacityByRainfallPercent(
		Double currentCapacity,
		Double rainfall
	) {
		if (currentCapacity == null || currentCapacity == 0) {
			return 0;
		}
		return currentCapacity * (1 + (0.01 * rainfall)); // 강수량의 1%를 저수량에 추가
	}

	public List<PastWeatherResponseDto> getShortForecastWeatherRainfall(
		double latitude,
		double longitude,
		ReservoirCapacityController.Type type
	) {
		//오늘 자정부터 데이터가 필요한 경우 어제자 23시 발표된 내용으로 조회 필요
		LocalDateTime yesterDay = LocalDateTime.now()
											   .minusDays(1);
		String baseTime = "2300";

		List<ShortForcecastWeatherResponseDto> shortForcecastWeatherResponseDtos = weatherService.getShortForecastWeather(
			latitude,
			longitude,
			yesterDay,
			baseTime);

		Map<LocalDateTime, List<ShortForcecastWeatherResponseDto>> groupedData = shortForcecastWeatherResponseDtos.stream()
																												  .collect(
																													  Collectors.groupingBy(
																														  dto -> {
																															  LocalDateTime dateTime = LocalDateTime.of(
																																  LocalDate.parse(
																																	  dto.getFcstDate(),
																																	  DateTimeFormatter.ofPattern(
																																		  DATE_FORMAT)),
																																  LocalTime.parse(
																																	  dto.getFcstTime(),
																																	  DateTimeFormatter.ofPattern(
																																		  TIME_FORMAT)));
																															  switch (type) {
																																  case
																																	  DAY:
																																	  return dateTime.truncatedTo(
																																		  ChronoUnit.DAYS);
																																  case
																																	  HOUR:
																																	  return dateTime.truncatedTo(
																																		  ChronoUnit.HOURS);
																																  default:
																																	  throw new IllegalArgumentException(
																																		  "Invalid type: " + type);
																															  }
																														  }));

		List<PastWeatherResponseDto> responseDtoList = new ArrayList<>();
		groupedData.forEach((dateTime, items) -> {
			double totalRainfall;
			if (type == ReservoirCapacityController.Type.DAY) {
				totalRainfall = items.stream()
									 .mapToDouble(dto -> dto.getRain1Hour())
									 .sum();
			} else {
				totalRainfall = items.stream()
									 .findFirst()
									 .map(dto -> dto.getRain1Hour())
									 .orElse(0.0);
			}

			PastWeatherResponseDto responseDto = PastWeatherResponseDto.builder()
																	   .date(dateTime.toString())
																	   .sumRain(totalRainfall)
																	   .build();

			responseDtoList.add(responseDto);
		});
		return responseDtoList.stream()
							  .sorted(Comparator.comparing(PastWeatherResponseDto::getDate))
							  .collect(Collectors.toList());
	}

}
