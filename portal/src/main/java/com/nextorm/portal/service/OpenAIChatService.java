package com.nextorm.portal.service;

import com.nextorm.common.db.repository.dto.ParameterRawDataStatistics;
import com.nextorm.portal.ai.openai.OpenAIClient;
import com.nextorm.portal.ai.openai.dto.RecievedMessageDto;
import com.nextorm.portal.config.properties.OpenAIProperties;
import com.nextorm.portal.dto.parameter.ParameterResponseDto;
import com.nextorm.portal.dto.parameter.ParameterSearchRequestDto;
import com.nextorm.portal.dto.parameterdata.ParameterDataTrendDto;
import com.nextorm.portal.dto.tool.ToolResponseDto;
import com.nextorm.portal.dto.tool.ToolSearchRequestDto;
import com.nextorm.portal.restapi.OpenStreetMapRestApi;
import com.nextorm.portal.restapi.dto.openstreetmap.ReverseGeocodingDto;
import com.nextorm.portal.service.parameter.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OpenAIChatService {

	private final OpenAIProperties openAIProperties;
	private final ToolService toolService;
	private final ParameterService parameterService;
	private final ParameterDataService parameterDataService;
	private final OpenStreetMapRestApi openStreetMapRestApi;

	public RecievedMessageDto sendMessage(
		String threadId,
		String message
	) {
		OpenAIProperties.Assistant assistantProps = openAIProperties.getAssistant();
		OpenAIClient client = OpenAIClient.builder(assistantProps.getApiKey(), assistantProps.getId())
										  .threadId(threadId)
										  .toolOutputHandler((function, arguments) -> {
											  Object data = null;
											  OpenAIClient.ToolOutputHandler.OutputResult.OutputResultBuilder builder = OpenAIClient.ToolOutputHandler.OutputResult.builder();
											  switch (function) {
												  case "GetAllEquipments": {
													  data = this.getTools();
													  break;
												  }
												  case "GetEquipmentParameters": {
													  data = this.getParametersByToolId(arguments);
													  break;
												  }
												  case "GetEquipmentParameterDataStatistics": {
													  data = this.getParameterDataStatistics(arguments);
													  break;
												  }
												  case "GetEquipmentParameterDataChart": {
													  data = this.getParameterDataTrend(arguments);
													  builder.isExtraData(true);
													  break;
												  }
												  case "GetEquipmentStatus": {
													  data = this.getEquipmentStatus(arguments);
													  break;
												  }
											  }
											  return builder.result(data)
															.build();
										  })
										  .build();
		return client.sendMessage(message);
	}

	private List<ToolResponseDto> getTools() {
		ToolSearchRequestDto toolSearch = new ToolSearchRequestDto();
		return toolService.getTools(toolSearch);
	}

	private List<ParameterResponseDto> getParametersByToolId(Map<String, Object> arguments) {
		Long toolId = Long.valueOf(arguments.get("toolId")
											.toString());
		ParameterSearchRequestDto search = new ParameterSearchRequestDto();
		search.setToolId(toolId);
		return parameterService.getParameters(search);
	}

	private List<ParameterRawDataStatistics> getParameterDataStatistics(Map<String, Object> arguments) {
		Long parameterId = Long.valueOf(arguments.get("parameterId")
												 .toString());
		String fromStr = arguments.get("from")
								  .toString();
		String toStr = arguments.get("to")
								.toString();

		LocalDateTime from = this.convertISOToLocalDateTime(fromStr);
		LocalDateTime to = this.convertISOToLocalDateTime(toStr);
		return parameterDataService.getRawDataStatistics(Arrays.asList(parameterId), from, to);
	}

	private ParameterDataTrendDto getParameterDataTrend(Map<String, Object> arguments) {
		Long parameterId = Long.valueOf(arguments.get("parameterId")
												 .toString());
		String fromStr = arguments.get("from")
								  .toString();
		String toStr = arguments.get("to")
								.toString();
		LocalDateTime from = this.convertISOToLocalDateTime(fromStr);
		LocalDateTime to = this.convertISOToLocalDateTime(toStr);
		return parameterDataService.getParameterDataTrend(parameterId, from, to);
	}

	private Map<String, Object> getEquipmentStatus(Map<String, Object> arguments) {
		Long toolId = Long.valueOf(arguments.get("toolId")
											.toString());
		String fromStr = arguments.get("from")
								  .toString();
		String toStr = arguments.get("to")
								.toString();

		LocalDateTime from = this.convertISOToLocalDateTime(fromStr);
		LocalDateTime to = this.convertISOToLocalDateTime(toStr);

		Map<String, Object> result = new HashMap<>();
		ParameterResponseDto latitude = parameterService.getParameterByNameAndToolId(toolId, "latitude");
		ParameterResponseDto longitude = parameterService.getParameterByNameAndToolId(toolId, "longitude");
		ParameterResponseDto ctrBatSoc = parameterService.getParameterByNameAndToolId(toolId, "ctr_bat_soc");
		ParameterResponseDto ctrBatV = parameterService.getParameterByNameAndToolId(toolId, "ctr_bat_v");
		ParameterResponseDto velocity = parameterService.getParameterByNameAndToolId(toolId, "velocity");
		ParameterResponseDto tempDegC = parameterService.getParameterByNameAndToolId(toolId, "temp_deg_c");

		List<ParameterDataTrendDto> parameterDataTrends = parameterDataService.getParameterDataTrend(Arrays.asList(
			latitude.getId(),
			longitude.getId()), from, to);

		int size = parameterDataTrends.stream()
									  .mapToInt(parameterData -> parameterData.getRawDatas()
																			  .size())
									  .min()
									  .orElse(0);

		if (size > 0) {
			double movedMeters = IntStream.range(0, size - 1)
										  .mapToDouble(index -> {
											  Double prevLongitude = (Double)parameterDataTrends.get(0)
																								.getRawDatas()
																								.get(index)
																								.getValue();
											  Double prevLatitude = (Double)parameterDataTrends.get(1)
																							   .getRawDatas()
																							   .get(index)
																							   .getValue();
											  Double nextLongitude = (Double)parameterDataTrends.get(0)
																								.getRawDatas()
																								.get(index + 1)
																								.getValue();
											  Double nextLatitude = (Double)parameterDataTrends.get(1)
																							   .getRawDatas()
																							   .get(index + 1)
																							   .getValue();
											  return calculateDistanceByCoordinates(prevLatitude,
												  prevLongitude,
												  nextLatitude,
												  nextLongitude);
										  })
										  .sum();
			result.put("movedMeters", movedMeters);

			Double firstLatitude = (Double)parameterDataTrends.get(0)
															  .getRawDatas()
															  .get(0)
															  .getValue();
			Double firstLongitude = (Double)parameterDataTrends.get(1)
															   .getRawDatas()
															   .get(0)
															   .getValue();
			Double lastLatitude = (Double)parameterDataTrends.get(0)
															 .getRawDatas()
															 .get(size - 1)
															 .getValue();
			Double lastLongitude = (Double)parameterDataTrends.get(1)
															  .getRawDatas()
															  .get(size - 1)
															  .getValue();

			ReverseGeocodingDto firstCall = openStreetMapRestApi.reverseGeocoding(firstLatitude, firstLongitude);
			ReverseGeocodingDto lastCall = openStreetMapRestApi.reverseGeocoding(lastLatitude, lastLongitude);

			String fromAddress = firstCall.getDisplayName();
			String toAddress = lastCall.getDisplayName();
			result.put("startingPosition", fromAddress);
			result.put("endingPosition", toAddress);
		}

		List<Long> parameterIds = Arrays.asList(ctrBatSoc.getId(),
			ctrBatV.getId(),
			velocity.getId(),
			ctrBatV.getId(),
			tempDegC.getId());

		List<ParameterRawDataStatistics> parameterRawDataStatistics = parameterDataService.getRawDataStatistics(
			parameterIds,
			from,
			to);

		result.put("statistics", parameterRawDataStatistics);

		return result;
	}

	private LocalDateTime convertISOToLocalDateTime(String isoString) {
		ZonedDateTime fromZoneDateTime = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME);
		LocalDateTime localDateTime = fromZoneDateTime.withZoneSameInstant(ZoneId.systemDefault())
													  .toLocalDateTime();
		return localDateTime;
	}

	public Double calculateDistanceByCoordinates(
		double lat1,
		double lon1,
		double lat2,
		double lon2
	) {
		Double theta = lon1 - lon2;
		Double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(
			lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515 * 1609.344;

		return dist; //단위 meter
	}

	// 10진수를 radian(라디안)으로 변환
	private Double deg2rad(Double deg) {
		return (deg * Math.PI / 180.0);
	}

	//radian(라디안)을 10진수로 변환
	private Double rad2deg(Double rad) {
		return (rad * 180 / Math.PI);
	}
}
