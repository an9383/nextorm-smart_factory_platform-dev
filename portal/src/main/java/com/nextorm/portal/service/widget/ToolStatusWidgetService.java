package com.nextorm.portal.service.widget;

import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.DcpConfigRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutResponseDto;
import com.nextorm.portal.dto.weather.VeryShortForcecastWeatherResponseDto;
import com.nextorm.portal.dto.widget.ToolStatusWidgetResponseDto;
import com.nextorm.portal.service.ReservoirLayoutService;
import com.nextorm.portal.service.WeatherService;
import com.nextorm.portal.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class ToolStatusWidgetService {
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterRepository parameterRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final WeatherService weatherService;
	private final ReservoirLayoutService reservoirLayoutService;
	private final ApplicationContext applicationContext;

	private static final String BATTERY_REMAINING = "ctr_bat_soc";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String VELOCITY = "velocity";

	public ToolStatusWidgetResponseDto getToolStatusWidgetData(Long toolId) {
		ToolStatusWidgetResponseDto widgetResponse = new ToolStatusWidgetResponseDto();
		List<Parameter> parameters = parameterRepository.findByToolId(toolId);
		List<DcpConfig> dcpConfigs = dcpConfigRepository.findByToolId(toolId);
		List<Long> parameterIds = parameters.stream()
											.map(BaseEntity::getId)
											.toList();
		LocalDateTime oneHourAgo = LocalDateTime.now()
												.minusHours(1);
		List<ParameterData> latestParameterData = parameterDataRepository.findLatestParameterData(parameterIds,
			oneHourAgo);

		Map<String, Parameter> parameterMap = mapParametersByName(parameters);
		if (!latestParameterData.isEmpty()) {
			updateWidgetData(widgetResponse, dcpConfigs, latestParameterData, parameterMap);
		}

		//GPS 좌표와 속도 파라미터의 1시간동안 데이터를 가져온다.
		List<ParameterData> pastHourData = parameterDataRepository.findByParameterIdInAndTraceAtBetween(Arrays.asList(
			parameterMap.get(VELOCITY)
						.getId()), oneHourAgo, LocalDateTime.now(), Sort.by(Sort.Direction.ASC, "traceAt"));

		if (!pastHourData.isEmpty()) {
			updateVelocityAndLocationData(widgetResponse, pastHourData, parameterMap);
		}

		ToolStatusWidgetService proxy = applicationContext.getBean(ToolStatusWidgetService.class);
		VeryShortForcecastWeatherResponseDto weatherResponseDto = null;
		if (widgetResponse.getLatitude() != null && widgetResponse.getLongitude() != null) {
			weatherResponseDto = proxy.updateWeatherData(toolId,
				widgetResponse.getLatitude(),
				widgetResponse.getLongitude());
		}
		if (weatherResponseDto != null) {
			widgetResponse.setSkyCondition(weatherResponseDto.getSkyCondition());
			widgetResponse.setPrecipitationFoam(weatherResponseDto.getPrecipitationFoam());
			widgetResponse.setTemperature(weatherResponseDto.getTemperature());
		}

		List<ReservoirLayoutResponseDto> reservoirLayoutResponseDtos = reservoirLayoutService.getReservoirLayout(
			Collections.singletonList(toolId));
		String markers = reservoirLayoutResponseDtos.get(0)
													.getData();
		widgetResponse.setMarkers(markers);

		return widgetResponse;
	}

	@Cacheable(value = "getWeatherData", key = "'_' + #toolId")
	public VeryShortForcecastWeatherResponseDto updateWeatherData(
		Long toolId,
		double latitude,
		double longitude
	) {
		try {
			return weatherService.getVeryShortForecastWeather(latitude, longitude)
								 .stream()
								 .min(Comparator.comparingInt(v -> Integer.parseInt(v.getFcstTime())))
								 .orElseThrow(() -> new NoSuchElementException("No weather data found"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 ** DCPConfig Interval * 3(초) 보다 이전인 데이터는 데이터 수집 상태 불량,
	 ** DCPConfig Interval * 3(초) 이내인 데이터는 양호로 처리
	 */
	private void updateWidgetData(
		ToolStatusWidgetResponseDto response,
		List<DcpConfig> dcpConfigs,
		List<ParameterData> latestParameterData,
		Map<String, Parameter> parameterMap
	) {
		int goodDataCount = 0;
		int badDataCount = 0;
		List<String> goodParameter = new ArrayList<>();
		List<String> badParameter = new ArrayList<>();
		LocalDateTime currentDateTime = LocalDateTime.now();
		for (String paramName : parameterMap.keySet()) {
			Parameter parameter = parameterMap.get(paramName);
			DcpConfig dcpConfig = findDcpConfigForParameter(dcpConfigs, parameter.getId());
			Optional<ParameterData> matchingParameterData = latestParameterData.stream()
																			   .filter(v -> v.getParameterId()
																							 .equals(parameter.getId()))
																			   .findFirst();
			if (matchingParameterData.isPresent()) {
				updateResponseData(response, matchingParameterData.get(), parameterMap);
				if (dcpConfig != null && isDataRecent(matchingParameterData.get(), dcpConfig, currentDateTime)) {
					goodDataCount++;
					goodParameter.add(paramName);
				}
			} else {
				badDataCount++;
				badParameter.add(paramName);
			}
		}

		response.setGoodCnt(goodDataCount);
		response.setBadCnt(badDataCount);
		response.setGoodParameter(goodParameter);
		response.setBadParameter(badParameter);
	}

	/*
	 	현재 데이터의 TraceAt 시간이 (현재시간 - Data Interval * 3) 보다 안에있는지 체크한다.
	 */
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

	/*
		남은 배터리 및 위도경도 데이터를 Set한다.
	 */
	private void updateResponseData(
		ToolStatusWidgetResponseDto response,
		ParameterData data,
		Map<String, Parameter> parameterMap
	) {
		if (data.getParameterId()
				.equals(parameterMap.get(BATTERY_REMAINING)
									.getId())) {
			response.setBatteryRemaining(data.getDValue()
											 .intValue());
			response.setLatitude(data.getLatitudeValue());
			response.setLongitude(data.getLongitudeValue());
		}
	}

	private Map<String, Parameter> mapParametersByName(List<Parameter> parameters) {
		Map<String, Parameter> parameterMap = new HashMap<>();
		for (Parameter parameter : parameters) {
			parameterMap.put(parameter.getName(), parameter);
		}
		return parameterMap;
	}

	/*
	 **	최근 1시간동안의 데이터로 좌표 시작과 끝지점 거리 계산 및 평균속도, 최고속도, 최저속도 계산
	 */
	private void updateVelocityAndLocationData(
		ToolStatusWidgetResponseDto response,
		List<ParameterData> pastHourData,
		Map<String, Parameter> parameterMap
	) {
		List<Double> velocities = new ArrayList<>();
		List<Double> latitudes = new ArrayList<>();
		List<Double> longitudes = new ArrayList<>();
		for (ParameterData data : pastHourData) {
			velocities.add(data.getDValue() * 60);

			latitudes.add(data.getLatitudeValue());

			longitudes.add(data.getLongitudeValue());
		}
		if (!velocities.isEmpty()) {

			response.setAvgVelocity(formatDouble(average(velocities)));
			response.setMaxVelocity(formatDouble(Collections.max(velocities)));
			response.setMinVelocity(formatDouble(Collections.min(velocities)));
		}

		response.setDistance(formatDouble(calculateTotalDistance(latitudes, longitudes)));

	}

	private double formatDouble(double doubleValue) {
		DecimalFormat df = new DecimalFormat("#.##");
		String formatted = df.format(doubleValue);
		return Double.parseDouble(formatted);
	}

	private double average(List<Double> values) {
		return values.stream()
					 .mapToDouble(v -> v)
					 .average()
					 .orElse(Double.NaN);
	}

	/*
		1시간 데이터의 위도 경도 전체 거리 계산 후 합을 Return
	 */
	private double calculateTotalDistance(
		List<Double> latitudes,
		List<Double> longitudes
	) {
		return IntStream.range(0, latitudes.size() - 1)
						.filter(i -> latitudes.get(i) != null && latitudes.get(i + 1) != null && longitudes.get(i) != null && longitudes.get(
							i + 1) != null)
						.mapToDouble(i -> GeoUtil.calculateDistanceByHaversine(latitudes.get(i),
							longitudes.get(i),
							latitudes.get(i + 1),
							longitudes.get(i + 1)))
						.sum();
	}
}