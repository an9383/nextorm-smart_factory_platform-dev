package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "parameter_data", indexes = {
	@Index(name = "parameter_data_search_complex_index", columnList = "parameter_id, trace_at desc")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuperBuilder
public class ParameterData extends BaseEntity {
	@Column(name = "parameter_id")
	private Long parameterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "data_type", columnDefinition = "varchar(20)")
	private Parameter.DataType dataType;

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

	@Column(name = "d_value", columnDefinition = "Decimal(15,5)")
	private Double dValue;

	@Column(name = "s_value")
	private String sValue;

	@Column(name = "i_value")
	private Integer iValue;

	@Column(name = "image_value", columnDefinition = "varchar(500)")
	private String imageValue;

	@Column(name = "trace_at")
	private LocalDateTime traceAt;

	@Column(name = "is_ctrl_limit_over", length = 1)
	@Comment("Ctrl Limit Over 여부")
	@Convert(converter = BooleanToStringConverter.class)
	private boolean isCtrlLimitOver;

	@Column(name = "is_spec_limit_over", length = 1)
	@Comment("Spec Limit Over 여부")
	@Convert(converter = BooleanToStringConverter.class)
	private boolean isSpecLimitOver;

	@Column(name = "latitude_value")
	@Comment("위도 데이터 값")
	private Double latitudeValue;

	@Column(name = "longitude_value")
	@Comment("경도 데이터 값")
	private Double longitudeValue;

	public static ParameterData createOf(
		Parameter parameter,
		Object value,
		LocalDateTime traceAt,
		Double latitudeValue,
		Double longitudeValue
	) {
		ParameterDataBuilder<?, ?> builder = ParameterData.builder()
														  .parameterId(parameter.getId())
														  .dataType(parameter.getDataType())
														  .target(parameter.getTarget())
														  .traceAt(traceAt)
														  .latitudeValue(latitudeValue)
														  .longitudeValue(longitudeValue);

		switch (parameter.getDataType()) {
			case DOUBLE -> builder.dValue(Double.parseDouble(value.toString()));
			case INTEGER -> {
				double d = Double.parseDouble(value.toString());
				builder.iValue((int)d);
			}
			case STRING -> builder.sValue(value.toString());
			case IMAGE -> builder.imageValue(value.toString());
		}

		if (parameter.isSpecAvailable()) {
			builder.ucl(parameter.getUcl())
				   .lcl(parameter.getLcl())
				   .usl(parameter.getUsl())
				   .lsl(parameter.getLsl());
		}

		ParameterData parameterData = builder.build();

		if (parameter.isSpecAvailable()) {
			parameterData.calculateLimitOver();
		}

		return parameterData;
	}

	public Object getValue(Parameter.DataType dataType) {
		if (dataType == Parameter.DataType.DOUBLE) {
			return dValue;
		} else if (dataType == Parameter.DataType.INTEGER) {
			return iValue;
		} else if (dataType == Parameter.DataType.STRING) {
			return sValue;
		} else {
			return imageValue;
		}
	}

	public Object getValue() {
		return getValue(this.getDataType());
	}

	public boolean isNumberData() {
		return dataType == Parameter.DataType.DOUBLE || dataType == Parameter.DataType.INTEGER;
	}

	public Number getNumberValue() {
		return switch (dataType) {
			case DOUBLE -> dValue;
			case INTEGER -> iValue;
			case STRING, IMAGE -> throw new IllegalArgumentException("지원되지 않는 파라미터 타입 입니다.");
		};
	}

	private void calculateLimitOver() {
		isCtrlLimitOver = isUclOver() || isLclOver();
		isSpecLimitOver = isUslOver() || isLslOver();

	}

	public boolean isSpecOver() {
		return isUslOver() || isUclOver() || isLclOver() || isLslOver();
	}

	private boolean isUslOver() {
		if (usl == null) {
			return false;
		}

		return switch (dataType) {
			case DOUBLE -> dValue > usl;
			case INTEGER -> iValue > usl;
			case STRING, IMAGE -> false;
		};
	}

	private boolean isUclOver() {
		if (ucl == null) {
			return false;
		}

		return switch (dataType) {
			case DOUBLE -> dValue > ucl;
			case INTEGER -> iValue > ucl;
			case STRING, IMAGE -> false;
		};
	}

	private boolean isLclOver() {
		if (lcl == null) {
			return false;
		}

		return switch (dataType) {
			case DOUBLE -> dValue < lcl;
			case INTEGER -> iValue < lcl;
			case STRING, IMAGE -> false;
		};
	}

	private boolean isLslOver() {
		if (lsl == null) {
			return false;
		}

		return switch (dataType) {
			case DOUBLE -> dValue < lsl;
			case INTEGER -> iValue < lsl;
			case STRING, IMAGE -> false;
		};
	}

	public FaultHistory toFaultHistory() {
		return FaultHistory.builder()
						   .parameterId(parameterId)
						   .paramValue(getValue(dataType) + "")
						   .faultAt(traceAt)

						   .target(target)
						   .lsl(lsl)
						   .lcl(lcl)
						   .usl(usl)
						   .ucl(ucl)

						   .isLslOver(isLslOver())
						   .isLclOver(isLclOver())
						   .isUslOver(isUslOver())
						   .isUclOver(isUclOver())

						   .isCtrlLimitOver(isCtrlLimitOver)
						   .isSpecLimitOver(isSpecLimitOver)
						   .build();
	}
}
