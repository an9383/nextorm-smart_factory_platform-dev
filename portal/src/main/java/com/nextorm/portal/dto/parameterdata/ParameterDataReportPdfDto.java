package com.nextorm.portal.dto.parameterdata;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.SummaryData;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ParameterDataReportPdfDto {
	private String searchStartDate;
	private String searchEndDate;
	private String toolLocation;
	private String toolName;
	private String note;
	private String periodType;
	private List<ParameterSummaryData> parameterSummaryDatas = new ArrayList<>();

	@Data
	@Builder
	public static class ParameterSummaryData {
		private Long parameterId;
		private String parameterName;
		private Parameter.DataType dataType;
		private String controlLimit;
		private String specLimit;
		private List<StatisticsData> statisticsDatas = new ArrayList<>();

		public Double getTotalAvg() {
			int realCount = statisticsDatas.stream()
										   .mapToInt(StatisticsData::getRealDataCount)
										   .sum();
			double avgSum = statisticsDatas.stream()
										   .mapToDouble(StatisticsData::getSum)
										   .sum();
			return avgSum / realCount;
		}

		public Double getMin() {
			return statisticsDatas.stream()
								  .mapToDouble(StatisticsData::getMin)
								  .min()
								  .getAsDouble();
		}

		public Double getMax() {
			return statisticsDatas.stream()
								  .mapToDouble(StatisticsData::getMax)
								  .max()
								  .getAsDouble();
		}

		public Double getMedian() {
			return (getMin() + getMax()) / 2;
		}

	}

	@Getter
	@Builder
	public static class StatisticsData {
		private String startTime;
		private String endTime;
		private Double max;
		private Double q3;
		private Double median;
		private Double q1;
		private Double min;
		private Double avg;
		private String specLimit;
		private String controlLimit;
		private double sum;
		private int realDataCount;
	}

	public static ParameterDataReportPdfDto of(
		String from,
		String to,
		String locationName,
		String toolName,
		String note,
		String translatedPeriodType,
		List<ParameterSummaryData> parameterSummaryDatas
	) {
		return ParameterDataReportPdfDto.builder()
										.searchStartDate(from)
										.searchEndDate(to)
										.toolLocation(locationName)
										.toolName(toolName)
										.note(note)
										.periodType(translatedPeriodType)
										.parameterSummaryDatas(parameterSummaryDatas)
										.build();
	}

	public static ParameterSummaryData of(
		Parameter parameter,
		Map<String, String> languageMap,
		List<StatisticsData> statisticsData
	) {
		String[] parameterLimit = convertSpecLimit(parameter.getLsl(),
			parameter.getUsl(),
			parameter.getLcl(),
			parameter.getUcl());
		String parameterSpecLimit = parameterLimit[0];
		String parameterControlLimit = parameterLimit[1];

		String parameterName = languageMap.get("param." + parameter.getName()) != null
							   ? languageMap.get("param." + parameter.getName())
							   : parameter.getName();

		return ParameterDataReportPdfDto.ParameterSummaryData.builder()
															 .parameterId(parameter.getId())
															 .parameterName(parameterName)
															 .dataType(parameter.getDataType())
															 .controlLimit(parameterControlLimit)
															 .specLimit(parameterSpecLimit)
															 .statisticsDatas(statisticsData)
															 .build();
	}

	public static StatisticsData of(
		SummaryData summaryData,
		DateTimeFormatter dateFormat
	) {
		String[] limitValue = convertSpecLimit(summaryData.getLsl(),
			summaryData.getUsl(),
			summaryData.getLcl(),
			summaryData.getUcl());
		String specLimit = limitValue[0];
		String controlLimit = limitValue[1];
		return ParameterDataReportPdfDto.StatisticsData.builder()
													   .startTime(summaryData.getSumStartBaseAt()
																			 .format(dateFormat))
													   .endTime(summaryData.getSumEndBaseAt()
																		   .format(dateFormat))
													   .max(summaryData.getMax())
													   .q3(summaryData.getQ3())
													   .median(summaryData.getMedian())
													   .q1(summaryData.getQ1())
													   .min(summaryData.getMin())
													   .avg(summaryData.getAvg())
													   .sum(summaryData.getSum())
													   .realDataCount(summaryData.getRealDataCount())
													   .specLimit(specLimit)
													   .controlLimit(controlLimit)
													   .build();
	}

	private static String[] convertSpecLimit(
		Double lsl,
		Double usl,
		Double lcl,
		Double ucl
	) {
		String lslValue = lsl == null
						  ? ""
						  : String.valueOf(lsl);
		String uslValue = usl == null
						  ? ""
						  : String.valueOf(usl);

		String specLimit = lslValue + "~" + uslValue;

		specLimit = specLimit.equals("~")
					? ""
					: specLimit;

		String lclValue = lcl == null
						  ? ""
						  : String.valueOf(lcl);
		String uclValue = ucl == null
						  ? ""
						  : String.valueOf(ucl);

		String controlLimit = lclValue + "~" + uclValue;

		controlLimit = controlLimit.equals("~")
					   ? ""
					   : controlLimit;

		return new String[] {specLimit, controlLimit};
	}

}
