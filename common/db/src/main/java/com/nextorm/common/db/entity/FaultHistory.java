package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "fault_history", indexes = {
	@Index(name = "idx_fault_history_parameter_id_fault_at", columnList = "parameter_id, fault_at")})
@NoArgsConstructor
@SuperBuilder
@Getter
public class FaultHistory extends BaseEntity {
	@Column(name = "parameter_id")
	@Comment("파라미터 ID")
	private Long parameterId;

	@Column(name = "param_value")
	private String paramValue;

	@Column(name = "is_spec_limit_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("Spec Limit Over 발생 여부")
	private boolean isSpecLimitOver;

	@Column(name = "is_ctrl_limit_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("Ctrl Limit Over 발생 여부")
	private boolean isCtrlLimitOver;

	@Column(name = "target", columnDefinition = "Decimal(15,5)")
	private Double target;
	@Column(name = "ucl", columnDefinition = "Decimal(15,5)")
	private Double ucl;
	@Column(name = "lcl", columnDefinition = "Decimal(15,5)")
	private Double lcl;
	@Column(name = "usl", columnDefinition = "Decimal(15,5)")
	private Double usl;
	@Column(name = "lsl", columnDefinition = "Decimal(15,5)")
	private Double lsl;

	@Column(name = "is_lsl_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("LSL Over 여부")
	private boolean isLslOver;

	@Column(name = "is_lcl_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("LCL Over 여부")
	private boolean isLclOver;

	@Column(name = "is_ucl_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("UCL Over 여부")
	private boolean isUclOver;

	@Column(name = "is_Usl_over", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("USL Over 여부")
	private boolean isUslOver;

	@Column(name = "fault_at")
	@Comment("Fault 발생 일시")
	private LocalDateTime faultAt;

	public static FaultHistory create(
		Long parameterId,
		Parameter.DataType parameterDataType,
		ParameterData parameterData,
		LocalDateTime faultAt
	) {
		FaultHistory faultHistory = new FaultHistory();

		faultHistory.target = parameterData.getTarget();
		faultHistory.lcl = parameterData.getLcl();
		faultHistory.lsl = parameterData.getLsl();
		faultHistory.ucl = parameterData.getUcl();
		faultHistory.usl = parameterData.getUsl();
		faultHistory.parameterId = parameterId;
		faultHistory.faultAt = faultAt;

		// 스펙을 설정한 이후에 실행해야함
		faultHistory.checkSpec(parameterData, parameterDataType);

		// 아래 두 메소드 호출은 스펙 체크가 선행 되어야 함
		faultHistory.isCtrlLimitOver = faultHistory.checkCtrlLimitOver();
		faultHistory.isSpecLimitOver = faultHistory.checkSpecLimitOver();
		return faultHistory;
	}

	private void checkSpec(
		ParameterData parameterData,
		Parameter.DataType parameterDataType
	) {
		switch (parameterDataType) {
			case DOUBLE -> {
				paramValue = parameterData.getDValue() + "";
				isUslOver = (parameterData.getDValue() > usl);
				isUclOver = (parameterData.getDValue() > ucl);
				isLclOver = (parameterData.getDValue() < lcl);
				isLslOver = (parameterData.getDValue() < lsl);
			}
			case INTEGER -> {
				paramValue = (parameterData.getIValue() + "");
				isUslOver = (parameterData.getIValue() > usl);
				isUclOver = (parameterData.getIValue() > ucl);
				isLclOver = (parameterData.getIValue() < lcl);
				isLslOver = (parameterData.getIValue() < lsl);
			}
			default -> throw new IllegalArgumentException("지원되지 않는 파라미터 타입: " + parameterDataType);
		}
	}

	private boolean checkCtrlLimitOver() {
		return isLclOver || isUclOver;
	}

	private boolean checkSpecLimitOver() {
		return isLslOver || isUslOver;
	}

	public boolean isOverSpec() {
		return isUslOver || isUclOver || isLclOver || isLslOver;
	}
}
