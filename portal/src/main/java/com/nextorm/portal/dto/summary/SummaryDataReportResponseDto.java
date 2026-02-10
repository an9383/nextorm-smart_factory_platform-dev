package com.nextorm.portal.dto.summary;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.SummaryData;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SummaryDataReportResponseDto {
	private Long parameterId;
	private String name;
	private Parameter.DataType dataType;

	private List<SummaryReportData> summaryReportDatas = new ArrayList<>();

	@Data
	@Builder
	public static class SummaryReportData {
		private LocalDateTime startTime;
		private LocalDateTime endTime;
		private Double max;
		private Double q3;
		private Double median;
		private Double q1;
		private Double min;
		private Double avg;
		private String specLimit;
		private String controlLimit;
	}

	public static SummaryDataReportResponseDto.SummaryReportData of(
		SummaryData summaryData
	) {
		String lsl = summaryData.getLsl() == null
					 ? ""
					 : String.valueOf(summaryData.getLsl());
		String usl = summaryData.getUsl() == null
					 ? ""
					 : String.valueOf(summaryData.getUsl());

		String specLimit = lsl + "~" + usl;
		specLimit = specLimit.equals("~")
					? ""
					: specLimit;

		String lcl = summaryData.getLcl() == null
					 ? ""
					 : String.valueOf(summaryData.getLcl());

		String ucl = summaryData.getUcl() == null
					 ? ""
					 : String.valueOf(summaryData.getUcl());

		String controlLimit = lcl + "~" + ucl;

		controlLimit = controlLimit.equals("~")
					   ? ""
					   : controlLimit;

		return SummaryDataReportResponseDto.SummaryReportData.builder()
															 .max(summaryData.getMax())
															 .q3(summaryData.getQ3())
															 .median(summaryData.getMedian())
															 .q1(summaryData.getQ1())
															 .min(summaryData.getMin())
															 .avg(summaryData.getAvg())
															 .startTime(summaryData.getSumStartBaseAt())
															 .endTime(summaryData.getSumEndBaseAt())
															 .startTime(summaryData.getSumStartBaseAt())
															 .endTime(summaryData.getSumEndBaseAt())
															 .specLimit(specLimit)
															 .controlLimit(controlLimit)
															 .build();
	}
}
