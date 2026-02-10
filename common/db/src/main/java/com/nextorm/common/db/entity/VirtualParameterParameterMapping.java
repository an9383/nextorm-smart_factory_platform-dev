package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

/**
 * 가상 파라미터 생성시 선택한 파라미터 매핑 정보
 */
@Entity
@Table(name = "virtual_parameter_parameter_mappings")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualParameterParameterMapping extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "virtual_parameter_id")
	@Comment("가상 파라미터")
	private Parameter virtualParameter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parameter_id")
	@Comment("매핑된 파라미터")
	private Parameter parameter;

	@Convert(converter = BooleanToStringConverter.class)
	@Column(name = "is_using_calculation", length = 1)
	@Comment("가상 파라미터 계산에 사용 여부")
	private boolean isUsingCalculation;

	public static VirtualParameterParameterMapping create(
		Parameter virtualParameter,
		Parameter parameter,
		boolean isUsingCalculation
	) {
		return VirtualParameterParameterMapping.builder()
											   .virtualParameter(virtualParameter)
											   .parameter(parameter)
											   .isUsingCalculation(isUsingCalculation)
											   .build();
	}
}
