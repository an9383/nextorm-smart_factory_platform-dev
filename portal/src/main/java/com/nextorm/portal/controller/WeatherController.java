package com.nextorm.portal.controller;

import com.nextorm.portal.dto.weather.*;
import com.nextorm.portal.restapi.dto.openstreetmap.ReverseGeocodingDto;
import com.nextorm.portal.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
	private final WeatherService weatherService;

	@GetMapping("/presentWeather")
	public PresentWeatherResponseDto getPresentWeather(
		@RequestParam(name = "latitude") double latitude,
		@RequestParam(name = "longitude") double longitude
	) {
		return weatherService.getPresentWeather(latitude, longitude);
	}

	@GetMapping("/shortForecastWeather")
	public List<ShortForcecastWeatherResponseDto> getShortForecastWeather(
		@RequestParam(name = "latitude") double latitude,
		@RequestParam(name = "longitude") double longitude
	) {
		return weatherService.getShortForecastWeather(latitude, longitude);
	}

	@GetMapping("/veryShortForecastWeather")
	public List<VeryShortForcecastWeatherResponseDto> getVeryShortForecastWeather(
		@RequestParam(name = "latitude") double latitude,
		@RequestParam(name = "longitude") double longitude
	) {
		return weatherService.getVeryShortForecastWeather(latitude, longitude);
	}

	@GetMapping("/middleForcecastWeather")
	public MiddleForcecastWeatherResponseDto getMiddleForcecastWeather(
		@RequestParam(name = "regionId") String regionId
	) {
		return weatherService.getMiddleForcecastWeather(regionId);
	}

	@GetMapping("/middleTempWeather")
	public MiddleTempWeatherResponseDto getMiddleTempWeather(
		@RequestParam(name = "regionId") String regionId
	) {
		return weatherService.getMiddleTempWeather(regionId);
	}

	@GetMapping("/pastWeather")
	public List<PastWeatherResponseDto> getPastWeather(
		@RequestParam("stationId") String stationId,
		@RequestParam("startDt") String startDt,
		@RequestParam("endDt") String endDt
	) {
		return weatherService.getPastWeather(stationId, startDt, endDt);
	}

	@GetMapping("/reverseGeocoding")
	public ReverseGeocodingDto reverseGeocoding(
		@RequestParam("lat") double latitude,
		@RequestParam("lon") double longitude
	) {
		return weatherService.reverseGeocoding(latitude, longitude);
	}
}
