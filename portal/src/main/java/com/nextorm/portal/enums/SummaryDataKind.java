package com.nextorm.portal.enums;

import com.nextorm.common.db.entity.SummaryData;
import lombok.Getter;

@Getter
public enum SummaryDataKind {
	MIN("최소값"), MAX("최대값"), SUM("합"), AVG("평균"), MEDIAN("중앙값"), Q1("제1사분위수"), Q3("제3사분위수"), STD("표준편차"), REAL_DATA_COUNT(
		"실제 데이터 개수"), FAULT_COUNT("Fault Count"), CTRL_LIMIT_OVER_COUNT("Control Limit Over Count"), SPEC_LIMIT_OVER_COUNT(
		"Spec Limit Over Count"), R("변위"), CP("CP"), CPK("CPK"), CPU("CPU"), CPL("CPL"), EWMA("EWMA");

	private final String displayName;

	SummaryDataKind(String displayName) {
		this.displayName = displayName;
	}

	public Double fromValue(SummaryData summaryData) {
		return switch (this) {
			case MIN -> summaryData.getMin();
			case MAX -> summaryData.getMax();
			case SUM -> summaryData.getSum();
			case AVG -> summaryData.getAvg();
			case MEDIAN -> summaryData.getMedian();
			case Q1 -> summaryData.getQ1();
			case Q3 -> summaryData.getQ3();
			case STD -> summaryData.getStd();
			case REAL_DATA_COUNT -> summaryData.getRealDataCount()
											   .doubleValue();
			case FAULT_COUNT -> summaryData.getFaultCount()
										   .doubleValue();
			case CTRL_LIMIT_OVER_COUNT -> summaryData.getCtrlLimitOverCount()
													 .doubleValue();
			case SPEC_LIMIT_OVER_COUNT -> summaryData.getSpecLimitOverCount()
													 .doubleValue();
			case R -> summaryData.getR();
			case CP -> summaryData.getCp();
			case CPK -> summaryData.getCpk();
			case CPU -> summaryData.getCpu();
			case CPL -> summaryData.getCpl();
			case EWMA -> summaryData.getEwma();
		};
	}
}
