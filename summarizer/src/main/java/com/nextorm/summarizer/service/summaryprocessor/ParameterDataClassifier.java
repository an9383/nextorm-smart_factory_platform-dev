package com.nextorm.summarizer.service.summaryprocessor;

import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.summarizer.SummaryRange;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ParameterData를 SummaryRange 단위로 분류하는 클래스
 */
public class ParameterDataClassifier {
	private final Map<SummaryPeriodType, LinkedHashMap<SummaryRange, List<ParameterData>>> summaryTypeBaseContainer = new HashMap<>();

	private LinkedHashMap<SummaryRange, List<ParameterData>> getTimeBaseContainer(SummaryPeriodType periodType) {
		return summaryTypeBaseContainer.computeIfAbsent(periodType, k -> new LinkedHashMap<>());
	}

	public void addParameterDataList(List<ParameterData> parameterDataList) {
		parameterDataList.forEach(this::addParameterData);
	}

	private void addParameterData(ParameterData parameterData) {
		for (SummaryPeriodType summaryType : SummaryPeriodType.values()) {
			LinkedHashMap<SummaryRange, List<ParameterData>> timeBaseContainer = getTimeBaseContainer(summaryType);

			LocalDateTime traceAt = parameterData.getTraceAt();
			SummaryRange summaryRange = SummaryRange.createBaseTimeIncludeSummaryRange(traceAt, summaryType);
			List<ParameterData> parameterDataList = timeBaseContainer.computeIfAbsent(summaryRange,
				k -> new ArrayList<>());
			parameterDataList.add(parameterData);
		}
	}

	/**
	 * SummaryRange 단위로 분류된 데이터를 반환
	 */
	public Map<SummaryRange, List<ParameterData>> getClassifiedParameterData() {
		LinkedHashMap<SummaryRange, List<ParameterData>> result = new LinkedHashMap<>();
		for (SummaryPeriodType summaryType : SummaryPeriodType.values()) {
			result.putAll(getTimeBaseContainer(summaryType));
		}
		return result;
	}
}
