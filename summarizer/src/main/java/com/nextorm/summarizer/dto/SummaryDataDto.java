package com.nextorm.summarizer.dto;

import com.nextorm.common.db.entity.HealthSummaryData;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SummaryDataDto {
	private Long id;
	private Long parameterId;
	private Double latitude;
	private Double longitude;
	private SummaryPeriodType periodType;
	private LocalDateTime trxStartAt;
	private LocalDateTime trxEndAt;
	private LocalDateTime sumStartBaseAt;
	private LocalDateTime sumEndBaseAt;

	private Double lcl;
	private Double ucl;
	private Double lsl;
	private Double usl;

	private Double startValue;
	private Double endValue;
	private Double min;
	private Double max;
	private Double sum;
	private Double avg;
	private Double median;
	private Double q1;
	private Double q3;
	private Double std;
	private Integer realDataCount;
	private Integer faultCount;
	private Integer ctrlLimitOverCount;
	private Integer specLimitOverCount;
	private Double r;
	private Double cp;
	private Double cpk;
	private Double cpu;
	private Double cpl;
	private Double ewma;

	public SummaryData toSummaryData() {
		return SummaryData.builder()
						  .parameterId(this.parameterId)
						  .periodType(this.periodType)
						  .trxStartAt(this.trxStartAt)
						  .trxEndAt(this.trxEndAt)
						  .sumStartBaseAt(this.sumStartBaseAt)
						  .sumEndBaseAt(this.sumEndBaseAt)
						  .lcl(this.lcl)
						  .ucl(this.ucl)
						  .lsl(this.lsl)
						  .usl(this.usl)
						  .startValue(this.startValue)
						  .endValue(this.endValue)
						  .min(this.min)
						  .max(this.max)
						  .sum(this.sum)
						  .avg(this.avg)
						  .median(this.median)
						  .q1(this.q1)
						  .q3(this.q3)
						  .std(this.std)
						  .realDataCount(this.realDataCount)
						  .faultCount(this.faultCount)
						  .ctrlLimitOverCount(this.ctrlLimitOverCount)
						  .specLimitOverCount(this.specLimitOverCount)
						  .r(this.r)
						  .cp(this.cp)
						  .cpk(this.cpk)
						  .cpu(this.cpu)
						  .cpl(this.cpl)
						  .ewma(this.ewma)
						  .build();
	}

	public HealthSummaryData toHealthSummaryData() {
		return HealthSummaryData.builder()
								.parameterId(this.parameterId)
								.latitude(this.latitude)
								.longitude(this.longitude)
								.periodType(this.periodType)
								.trxStartAt(this.trxStartAt)
								.trxEndAt(this.trxEndAt)
								.sumStartBaseAt(this.sumStartBaseAt)
								.sumEndBaseAt(this.sumEndBaseAt)
								.lcl(this.lcl)
								.ucl(this.ucl)
								.lsl(this.lsl)
								.usl(this.usl)
								.startValue(this.startValue)
								.endValue(this.endValue)
								.min(this.min)
								.max(this.max)
								.sum(this.sum)
								.avg(this.avg)
								.median(this.median)
								.q1(this.q1)
								.q3(this.q3)
								.std(this.std)
								.realDataCount(this.realDataCount)
								.faultCount(this.faultCount)
								.ctrlLimitOverCount(this.ctrlLimitOverCount)
								.specLimitOverCount(this.specLimitOverCount)
								.r(this.r)
								.cp(this.cp)
								.cpk(this.cpk)
								.cpu(this.cpu)
								.cpl(this.cpl)
								.ewma(ewma)
								.build();
	}
}
