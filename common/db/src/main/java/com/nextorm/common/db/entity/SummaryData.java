package com.nextorm.common.db.entity;

import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "summary_data", indexes = {
	@Index(name = "summary_data_search_complex_index", columnList = "parameter_id, period_type, trx_start_at desc")})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class SummaryData extends BaseEntity {

	@Column(name = "parameter_id")
	private Long parameterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "period_type", columnDefinition = "varchar(20)")
	@Comment("ONE_MINUTE, TEN_MINUTES, HOURLY, SIX_HOURLIES, DAILY")
	private SummaryPeriodType periodType;

	@Column(name = "trx_start_at")
	@Comment("데이터의 시작 시간")
	private LocalDateTime trxStartAt;

	@Column(name = "trx_end_at")
	@Comment("데이터의 종료 시간")
	private LocalDateTime trxEndAt;

	@Column(name = "sum_start_base_at")
	@Comment("Summary  작업을 위한 기준 시작 시간 : 예) HOURLY : 01:00:00")
	private LocalDateTime sumStartBaseAt;

	@Column(name = "sum_end_base_at")
	@Comment("Summary 작업을 위한 기준 종료 시간 : 예) HOURLY : 02:00:00")
	private LocalDateTime sumEndBaseAt;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("원본 데이터에서 사용된 LCL")
	private Double lcl;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("원본 데이터에서 사용된 UCL")
	private Double ucl;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("원본 데이터에서 사용된 LSL")
	private Double lsl;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("원본 데이터에서 사용된 USL")
	private Double usl;

	@Column(name = "start_value", columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 시작 Value")
	private Double startValue;

	@Column(name = "end_value", columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 끝 Value")
	private Double endValue;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값 중 최소값")
	private Double min;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값 중 최대값")
	private Double max;

	@Column(columnDefinition = "Decimal(25,5)")
	@Comment("기간 내 모든 값의 합")
	private Double sum;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값의 평균")
	private Double avg;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값의 중앙값")
	private Double median;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값의 제1사분위수")
	private Double q1;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값의 제3사분위수")
	private Double q3;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("기간 내 모든 값의 표본 집단의 표준편차")
	private Double std;

	@Column(name = "real_data_count")
	@Comment("실제 해당 시간 내의 데이터 총 개수")
	private Integer realDataCount;

	@Column(name = "fault_count")
	@Comment("Ctrl Limit Over + Spec Limit Over")
	private Integer faultCount;

	@Column(name = "ctrl_limit_over_count")
	@Comment("기간동안 발생된 Control Limit Over Count 수")
	private Integer ctrlLimitOverCount;

	@Column(name = "spec_limit_over_count")
	@Comment("기간동안 발생된 Spec Limit Over Count 수")
	private Integer specLimitOverCount;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("MIN의 MAX - MIN의 MIN 한 값 (변위)")
	private Double r;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("중앙값에 대한 CP ((USL - LSL)) / 6*STD_OF_MEDIAN")
	private Double cp;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("중앙값에 대한 CPK")
	private Double cpk;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("(USL-평균)/3σ, 규격내에서 공정의 분포가 치우침이 있을 경우의 공정 능력지수")
	private Double cpu;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("(평균-LSL)/3σ, 규격내에서 공정의 분포가 치우침이 있을 경우의  공정 능력지수")
	private Double cpl;

	@Column(columnDefinition = "Decimal(15,5)")
	@Comment("지수가중이동평균")
	private Double ewma;
}
