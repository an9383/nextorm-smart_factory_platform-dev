package com.nextorm.portal.service;

import com.nextorm.portal.common.exception.weather.WeatherCategoryNotFoundException;
import com.nextorm.portal.dto.weather.*;
import com.nextorm.portal.dto.weather.enums.PrecipitationFoam;
import com.nextorm.portal.dto.weather.enums.SkyCondition;
import com.nextorm.portal.dto.weather.enums.WindDirection;
import com.nextorm.portal.restapi.OpenStreetMapRestApi;
import com.nextorm.portal.restapi.WeatherRestApi;
import com.nextorm.portal.restapi.dto.openstreetmap.ReverseGeocodingDto;
import com.nextorm.portal.restapi.dto.weather.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WeatherService {
	private final OpenStreetMapRestApi openStreetMapRestApi;
	private final WeatherRestApi weatherRestApi;
	private static final String DATE_FORMAT = "yyyyMMdd";

	public PresentWeatherResponseDto getPresentWeather(
		double latitude,
		double longitude
	) {
		String[] xyConv = convertLatLngToXy(latitude, longitude);
		LocalDateTime now = LocalDateTime.now();
		int minute = now.getMinute();
		int regulationMin = 39;
		// 현시간이 40분 미만일 경우 이전시간 한시간 전의 정보로 요청
		if (minute < regulationMin) {
			now = now.minusHours(1);
		}
		now = now.withMinute(0);
		String baseDate = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String baseTime = now.format(DateTimeFormatter.ofPattern("HHmm"));

		PresentWeatherDto weatherDto = weatherRestApi.getPresentWeather(baseDate, baseTime, xyConv[0], xyConv[1]);
		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return null;
		}
		List<PresentWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
																 .getBody()
																 .getItems()
																 .getItem();

		PresentWeatherResponseDto responseItem = new PresentWeatherResponseDto();
		weatherDtoItems.forEach(item -> {
			String value = item.getObsrValue();

			switch (item.getCategory()) {
				case "PTY" -> responseItem.setPrecipitationFoam(PrecipitationFoam.of(value));
				case "REH" -> responseItem.setHumidity(value);
				case "RN1" -> responseItem.setRain1Hour(convertToPrecipitation(value));
				case "T1H" -> responseItem.setTemperature(value);
				case "UUU" -> responseItem.setEastWestWind(value);
				case "VEC" -> responseItem.setDirectionWind(WindDirection.of(Integer.parseInt(value)));
				case "VVV" -> responseItem.setNorthSouthWind(value);
				case "WSD" -> responseItem.setWindSpeed(value);
				default -> throw new WeatherCategoryNotFoundException();
			}
		});
		return responseItem;
	}

	public List<ShortForcecastWeatherResponseDto> getShortForecastWeather(
		double latitude,
		double longitude
	) {
		LocalDateTime now = LocalDateTime.now();
		String baseTime = convertToShortForecastTime(now.getHour());

		return this.getShortForecastWeather(latitude, longitude, now, baseTime);

	}

	public List<ShortForcecastWeatherResponseDto> getShortForecastWeather(
		double latitude,
		double longitude,
		LocalDateTime baseDate,
		String baseTimeHHmm
	) {
		String[] xyConv = convertLatLngToXy(latitude, longitude);
		String baseDateStr = baseDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String baseTime = baseTimeHHmm;

		ShortForecastWeatherDto weatherDto = weatherRestApi.getShortForecastWeather(baseDateStr,
			baseTime,
			xyConv[0],
			xyConv[1]);
		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return Collections.emptyList();
		}

		List<ShortForecastWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
																	   .getBody()
																	   .getItems()
																	   .getItem();

		return weatherDtoItems.stream()
							  .collect(Collectors.groupingBy(ShortForecastWeatherDto.Item::getFcstDate))
							  .values()
							  .stream()
							  .flatMap(list -> list.stream()
												   .collect(Collectors.groupingBy(ShortForecastWeatherDto.Item::getFcstTime))
												   .values()
												   .stream()
												   .map(subList -> {
													   ShortForcecastWeatherResponseDto dto = new ShortForcecastWeatherResponseDto();
													   dto.setFcstDate(subList.get(0)
																			  .getFcstDate());
													   dto.setFcstTime(subList.get(0)
																			  .getFcstTime());
													   subList.forEach(item -> {
														   String category = item.getCategory();
														   String value = item.getFcstValue();
														   switch (category) {
															   case "TMP" -> dto.setTemp1Hour(value);
															   case "PCP" ->
																   dto.setRain1Hour(convertToPrecipitation(value));
															   case "SNO" ->
																   dto.setSnow1Hour(convertToPrecipitation(value));
															   case "POP" -> dto.setRainPercent(value);
															   case "SKY" ->
																   dto.setSkyCondition(SkyCondition.of(value));
															   case "VEC" ->
																   dto.setDirectionWind(WindDirection.of(Integer.parseInt(
																	   value)));
															   case "WSD" -> dto.setWindSpeed(value);
															   case "UUU" -> dto.setEastWestWind(value);
															   case "VVV" -> dto.setNorthSouthWind(value);
															   case "REH" -> dto.setHumidity(value);
															   case "PTY" ->
																   dto.setPrecipitationFoam(PrecipitationFoam.of(value));
															   case "WAV" -> dto.setWave(value);
															   case "TMN" -> dto.setDayMinTemp(value);
															   case "TMX" -> dto.setDayMaxTemp(value);
															   default -> throw new WeatherCategoryNotFoundException();
														   }
													   });
													   return dto;
												   }))
							  .sorted(Comparator.comparing(ShortForcecastWeatherResponseDto::getFcstDate)
												.thenComparing(ShortForcecastWeatherResponseDto::getFcstTime))
							  .toList();
	}

	public List<VeryShortForcecastWeatherResponseDto> getVeryShortForecastWeather(
		double latitude,
		double longitude
	) {
		String[] xyConv = convertLatLngToXy(latitude, longitude);
		LocalDateTime now = LocalDateTime.now();
		int minute = now.getMinute();
		int regulationMin = 45;
		//현시간이 45분 이전이면 1시간 이전으로 조회
		if (minute < regulationMin) {
			now = now.minusHours(1);
		}
		now = now.withMinute(30);
		String baseDate = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String baseTime = now.format(DateTimeFormatter.ofPattern("HHmm"));
		VeryShortForecastWeatherDto weatherDto = weatherRestApi.getVeryShortForecastWeather(baseDate,
			baseTime,
			xyConv[0],
			xyConv[1]);
		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return Collections.emptyList();
		}
		List<VeryShortForecastWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
																		   .getBody()
																		   .getItems()
																		   .getItem();

		return weatherDtoItems.stream()
							  .collect(Collectors.groupingBy(VeryShortForecastWeatherDto.Item::getFcstTime))
							  .values()
							  .stream()
							  .map(fcstTimeItemMap -> {
								  VeryShortForcecastWeatherResponseDto dto = new VeryShortForcecastWeatherResponseDto();
								  fcstTimeItemMap.forEach(fcstTime -> {
									  dto.setFcstTime(fcstTime.getFcstTime());
									  String value = fcstTime.getFcstValue();
									  switch (fcstTime.getCategory()) {
										  case "T1H" -> dto.setTemperature(value);
										  case "RN1", "PCP" -> dto.setRain1Hour(convertToPrecipitation(value));
										  case "SKY" -> dto.setSkyCondition(SkyCondition.of(value));
										  case "UUU" -> dto.setEastWestWind(value);
										  case "VVV" -> dto.setNorthSouthWind(value);
										  case "REH" -> dto.setHumidity(value);
										  case "PTY" -> dto.setPrecipitationFoam(PrecipitationFoam.of(value));
										  case "LGT" -> dto.setLightning(value);
										  case "VEC" -> dto.setDirectionWind(WindDirection.of(Integer.parseInt(value)));
										  case "WSD" -> dto.setWindSpeed(value);
										  default -> throw new WeatherCategoryNotFoundException();
									  }
								  });
								  return dto;
							  })
							  .sorted(Comparator.comparing(VeryShortForcecastWeatherResponseDto::getFcstTime))
							  .toList();
	}

	public MiddleForcecastWeatherResponseDto getMiddleForcecastWeather(
		String regionId
	) {
		String time = getCurrentBaseTime();
		MiddleForcecastWeatherDto weatherDto = weatherRestApi.getMiddleForcecastWeather(regionId, time);

		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return null;
		}
		List<MiddleForcecastWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
																		 .getBody()
																		 .getItems()
																		 .getItem();

		return MiddleForcecastWeatherResponseDto.builder()
												.regId(weatherDtoItems.get(0)
																	  .getRegId())
												.rainAfter3Am(weatherDtoItems.get(0)
																			 .getRnSt3Am())
												.rainAfter3Pm(weatherDtoItems.get(0)
																			 .getRnSt3Pm())
												.rainAfter4Am(weatherDtoItems.get(0)
																			 .getRnSt4Am())
												.rainAfter4Pm(weatherDtoItems.get(0)
																			 .getRnSt4Pm())
												.rainAfter5Am(weatherDtoItems.get(0)
																			 .getRnSt5Am())
												.rainAfter5Pm(weatherDtoItems.get(0)
																			 .getRnSt5Pm())
												.rainAfter6Am(weatherDtoItems.get(0)
																			 .getRnSt6Am())
												.rainAfter6Pm(weatherDtoItems.get(0)
																			 .getRnSt6Pm())
												.rainAfter7Am(weatherDtoItems.get(0)
																			 .getRnSt7Am())
												.rainAfter7Pm(weatherDtoItems.get(0)
																			 .getRnSt7Pm())
												.rainAfter8(weatherDtoItems.get(0)
																		   .getRnSt8())
												.rainAfter9(weatherDtoItems.get(0)
																		   .getRnSt9())
												.rainAfter10(weatherDtoItems.get(0)
																			.getRnSt10())

												.weather3Am(weatherDtoItems.get(0)
																		   .getWf3Am())
												.weather3Pm(weatherDtoItems.get(0)
																		   .getWf3Pm())
												.weather4Am(weatherDtoItems.get(0)
																		   .getWf4Am())
												.weather4Pm(weatherDtoItems.get(0)
																		   .getWf4Pm())
												.weather5Am(weatherDtoItems.get(0)
																		   .getWf5Am())
												.weather5Pm(weatherDtoItems.get(0)
																		   .getWf5Pm())
												.weather6Am(weatherDtoItems.get(0)
																		   .getWf6Am())
												.weather6Pm(weatherDtoItems.get(0)
																		   .getWf6Pm())
												.weather7Am(weatherDtoItems.get(0)
																		   .getWf7Am())
												.weather7Pm(weatherDtoItems.get(0)
																		   .getWf7Pm())
												.weather8(weatherDtoItems.get(0)
																		 .getWf8())
												.weather9(weatherDtoItems.get(0)
																		 .getWf9())
												.weather10(weatherDtoItems.get(0)
																		  .getWf10())
												.build();
	}

	public MiddleTempWeatherResponseDto getMiddleTempWeather(
		String regionId
	) {
		String time = getCurrentBaseTime();
		MiddleTempWeatherDto weatherDto = weatherRestApi.getMiddleTempWeather(regionId, time);
		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return null;
		}
		List<MiddleTempWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
																	.getBody()
																	.getItems()
																	.getItem();

		return MiddleTempWeatherResponseDto.builder()
										   .regId(weatherDtoItems.get(0)
																 .getRegId())
										   .after3MinTemp(weatherDtoItems.get(0)
																		 .getTaMin3())
										   .after3MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax3())
										   .after4MinTemp(weatherDtoItems.get(0)
																		 .getTaMin4())
										   .after4MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax4())
										   .after5MinTemp(weatherDtoItems.get(0)
																		 .getTaMin5())
										   .after5MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax5())
										   .after6MinTemp(weatherDtoItems.get(0)
																		 .getTaMin6())
										   .after6MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax6())
										   .after7MinTemp(weatherDtoItems.get(0)
																		 .getTaMin7())
										   .after7MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax7())
										   .after8MinTemp(weatherDtoItems.get(0)
																		 .getTaMin8())
										   .after8MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax8())
										   .after9MinTemp(weatherDtoItems.get(0)
																		 .getTaMin9())
										   .after9MaxTemp(weatherDtoItems.get(0)
																		 .getTaMax9())
										   .after10MinTemp(weatherDtoItems.get(0)
																		  .getTaMin10())
										   .after10MaxTemp(weatherDtoItems.get(0)
																		  .getTaMax10())
										   .build();
	}

	public List<PastWeatherResponseDto> getPastWeather(
		String stationId,
		String startDt,
		String endDt
	) {
		PastWeatherDto weatherDto = weatherRestApi.getPastWeather(stationId, startDt, endDt);
		if (weatherDto.getResponse() == null || weatherDto.getResponse()
														  .getBody() == null) {
			return Collections.emptyList();
		}
		List<PastWeatherDto.Item> weatherDtoItems = weatherDto.getResponse()
															  .getBody()
															  .getItems()
															  .getItem();
		List<PastWeatherResponseDto> responseItems = new ArrayList<>();
		weatherDtoItems.forEach(dtoItem -> responseItems.add(PastWeatherResponseDto.builder()
																				   .stationName(dtoItem.getStnNm())
																				   .date(dtoItem.getTm()
																								.replace("-", ""))
																				   .avgTemp(dtoItem.getAvgTa())
																				   .minTemp(dtoItem.getMinTa())
																				   .maxTemp(dtoItem.getMaxTa())
																				   .avgWind(dtoItem.getAvgWs())
																				   .maxWind(dtoItem.getMaxWs())
																				   .sumRain(convertToPrecipitation(
																					   dtoItem.getSumRn()))
																				   .build()));
		return responseItems;
	}

	public ReverseGeocodingDto reverseGeocoding(
		double latitude,
		double longitude
	) {

		return openStreetMapRestApi.reverseGeocoding(latitude, longitude);
	}

	public String[] convertLatLngToXy(
		double latitude,
		double longitude
	) {
		final double RE = 6371.00877; // 지구 반경(km)
		final double GRID = 5.0; // 격자 간격(km)
		final double SLAT1 = 30.0; // 투영 위도1(degree)
		final double SLAT2 = 60.0; // 투영 위도2(degree)
		final double OLON = 126.0; // 기준점 경도(degree)
		final double OLAT = 38.0; // 기준점 위도(degree)
		final int XO = 43; // 기준점 X좌표(GRID)
		final int YO = 136; // 기1준점 Y좌표(GRID)

		final double DEGRAD = Math.PI / 180.0;

		double re = RE / GRID;
		double slat1 = SLAT1 * DEGRAD;
		double slat2 = SLAT2 * DEGRAD;
		double olon = OLON * DEGRAD;
		double olat = OLAT * DEGRAD;

		double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
		double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sf = (Math.pow(sf, sn) * Math.cos(slat1)) / sn;
		double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
		ro = (re * sf) / Math.pow(ro, sn);

		double ra = Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5);
		ra = (re * sf) / Math.pow(ra, sn);

		double theta = longitude * DEGRAD - olon;
		if (theta > Math.PI) {
			theta -= 2.0 * Math.PI;
		}
		if (theta < -Math.PI) {
			theta += 2.0 * Math.PI;
		}
		theta *= sn;

		long x = (long)Math.floor(ra * Math.sin(theta) + XO + 0.5);
		long y = (long)Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

		return new String[] {String.valueOf(x), String.valueOf(y)};
	}

	public String convertToShortForecastTime(int time) {
		return switch (time) {
			case 2, 3, 4 -> "0200";
			case 5, 6, 7 -> "0500";
			case 8, 9, 10 -> "0800";
			case 11, 12, 13 -> "1100";
			case 14, 15, 16 -> "1400";
			case 17, 18, 19 -> "1700";
			case 20, 21, 22 -> "2200";
			default -> "2300";
		};
	}

	public String getCurrentBaseTime() {
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		if (hour < 6) {
			now = now.minusDays(1)
					 .withHour(18)
					 .withMinute(0);
		} else if (hour < 18) {
			now = now.withHour(6)
					 .withMinute(0);
		} else {
			now = now.withHour(18)
					 .withMinute(0);
		}
		return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
	}

	public double convertToPrecipitation(String value) {
		if (value.equals("강수없음") || value.equals("적설없음") || value.isEmpty()) {
			return 0.0;
		} else if (value.contains("mm")) {
			if (value.contains("미만")) {
				value = value.trim()
							 .replace("미만", "")
							 .trim();
			}
			return Double.parseDouble(value.replace("mm", "")
										   .trim());
		} else {
			return Double.parseDouble(value);
		}
	}
}
