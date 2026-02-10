package com.nextorm.portal.enums;

import com.nextorm.common.db.entity.SummaryData;

import java.util.List;

public enum Operator {
	AVG, MAX, MIN;

	public double calculate(List<SummaryData> dailySummaryData) {
		return switch (this) {
			case AVG -> dailySummaryData.stream()
										.mapToDouble(SummaryData::getAvg)
										.average()
										.orElse(0);
			case MAX -> dailySummaryData.stream()
										.mapToDouble(SummaryData::getMax)
										.max()
										.orElse(0);
			case MIN -> dailySummaryData.stream()
										.mapToDouble(SummaryData::getMin)
										.min()
										.orElse(0);
		};
	}
}
