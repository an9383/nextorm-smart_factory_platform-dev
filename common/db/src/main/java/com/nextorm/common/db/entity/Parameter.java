package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "parameter")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Parameter extends BaseEntity {

	public enum DataType {
		DOUBLE("d_value"), STRING("s_value"), INTEGER("i_value"), IMAGE(null);

		private final String dataTableColumnName;

		DataType(String dataTableColumnName) {
			this.dataTableColumnName = dataTableColumnName;
		}

		public String getDataTableColumnName() {
			if (this == IMAGE) {
				throw new UnsupportedOperationException("이미지 타입은 데이터 테이블 컬럼명을 지원하지 않습니다.");
			}
			return dataTableColumnName;
		}
	}

	public enum Type {
		TRACE, EVENT, HEALTH, META_DATA
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", columnDefinition = "varchar(20)")
	private Type type;

	@Enumerated(EnumType.STRING)
	@Column(name = "data_type", columnDefinition = "varchar(20)")
	private DataType dataType;

	@Column(name = "is_spec_available", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("스펙 설정 여부")
	private boolean isSpecAvailable;

	@Column(name = "is_auto_spec", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("스펙 자동 계산 여부")
	private boolean isAutoSpec;

	@Column(name = "is_req_recalculate", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("스펙 자동 계산을 다시 계산할 지 여부")
	private boolean isReqRecalculate;

	@Column(name = "auto_calc_curr_cnt")
	@Comment("스펙 자동 계산 횟수")
	private Integer autoCalcCurrCnt;

	@Column(name = "auto_calc_period")
	@Comment("스펙 자동 계산 기간(일)")
	private Integer autoCalcPeriod;

	@Column(name = "unit")
	@Comment("단위")
	private String unit;

	@Column(name = "meta_value")
	@Comment("메타데이터")
	private String metaValue;

	@Column(columnDefinition = "Decimal(15,5)")
	private Double target;
	@Column(columnDefinition = "Decimal(15,5)")
	private Double ucl;
	@Column(columnDefinition = "Decimal(15,5)")
	private Double lcl;
	@Column(columnDefinition = "Decimal(15,5)")
	private Double usl;
	@Column(columnDefinition = "Decimal(15,5)")
	private Double lsl;

	@Column(name = "seq")
	private Integer order;

	@Column(name = "is_virtual")
	@Comment("가상 파라미터 여부")
	private boolean isVirtual;

	@Comment("가상 파라미터 생성 로직 (js)")
	@Column(name = "virtual_script", columnDefinition = "TEXT")
	private String virtualScript;

	@Comment("가상 파라미터 로직 생성 시 사용된 워크스페이스 정보 (json)")
	@Column(name = "virtual_workspace", columnDefinition = "TEXT")
	private String virtualWorkspace;

	@OneToMany(mappedBy = "virtualParameter", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Comment("가상 파라미터 생성시 선택한 파라미터 매핑 정보 (가상 파라미터에만 이 정보가 있어야 한다)")
	@Builder.Default
	private List<VirtualParameterParameterMapping> mappingParameters = new ArrayList<>();

	public void modify(Parameter parameter) {
		if (Objects.nonNull(parameter)) {
			this.name = parameter.getName();
			this.type = parameter.getType();
			this.dataType = parameter.getDataType();
			this.isSpecAvailable = parameter.isSpecAvailable();
			this.isAutoSpec = parameter.isAutoSpec();
			this.isReqRecalculate = parameter.isReqRecalculate();
			this.autoCalcCurrCnt = parameter.getAutoCalcCurrCnt();
			this.autoCalcPeriod = parameter.getAutoCalcPeriod();
			this.unit = parameter.getUnit();
			this.target = parameter.getTarget();
			this.ucl = parameter.getUcl();
			this.lcl = parameter.getLcl();
			this.usl = parameter.getUsl();
			this.lsl = parameter.getLsl();
			this.order = parameter.getOrder();
			this.metaValue = parameter.getMetaValue();
		}
	}

	/**
	 * 가상 파라미터 수정
	 *
	 * @param updateData: 수정할 파라미터 정보
	 */
	public void modifyVirtualParameter(
		Parameter updateData,
		List<Parameter> mappingParameters,
		Set<Long> calculationUsingParameterIdSet
	) {
		// null 체크 필요없음
		target = updateData.target;
		ucl = updateData.ucl;
		lcl = updateData.lcl;
		usl = updateData.usl;
		lsl = updateData.lsl;
		autoCalcPeriod = updateData.autoCalcPeriod;
		metaValue = updateData.metaValue;

		if (Objects.nonNull(updateData.name)) {
			name = updateData.name;
		}
		if (Objects.nonNull(updateData.type)) {
			type = updateData.type;
		}
		if (Objects.nonNull(updateData.dataType)) {
			dataType = updateData.dataType;
		}
		if (Objects.nonNull(updateData.order)) {
			order = updateData.order;
		}
		if (Objects.nonNull(updateData.virtualScript)) {
			virtualScript = updateData.virtualScript;
		}
		if (Objects.nonNull(updateData.virtualWorkspace)) {
			virtualWorkspace = updateData.virtualWorkspace;
		}
		if (updateData.isSpecAvailable() != isSpecAvailable) {
			isSpecAvailable = updateData.isSpecAvailable();
		}
		if (updateData.isAutoSpec() != isAutoSpec) {
			isAutoSpec = updateData.isAutoSpec();
		}
		if (updateData.isReqRecalculate() != isReqRecalculate) {
			isReqRecalculate = updateData.isReqRecalculate();
		}
		this.mappingParameters.clear();
		this.addMappingParameters(mappingParameters, calculationUsingParameterIdSet);
	}

	public void addMappingParameters(
		List<Parameter> parameters,
		Set<Long> calculationUsingParameterIdSet
	) {
		this.mappingParameters.addAll(parameters.stream()
												.map(parameter -> VirtualParameterParameterMapping.create(this,
													parameter,
													calculationUsingParameterIdSet.contains(parameter.getId())))
												.toList());
	}

	/**
	 * 스펙 정보 및 자동 스펙 계산 정보 수정<br/>
	 * # 자동 스펙 계산 정보: (계산 횟수, 재계산 여부)
	 */
	public void modifySpecsAndAutoCalculateInfo(
		double ucl,
		double lcl,
		double usl,
		double lsl
	) {
		this.ucl = ucl;
		this.lcl = lcl;
		this.usl = usl;
		this.lsl = lsl;
		this.isReqRecalculate = false;

		if (Objects.isNull(autoCalcCurrCnt)) {
			autoCalcCurrCnt = 1;
		} else {
			autoCalcCurrCnt++;
		}
	}

	public boolean isNumberType() {
		return dataType == DataType.DOUBLE || dataType == DataType.INTEGER;
	}

	/**
	 * 자동 스펙 계산 기간 잘못 설정여부
	 */
	public boolean isInvalidAutoCalcPeriod() {
		return autoCalcPeriod == null || autoCalcPeriod <= 0;
	}

	/**
	 * 수동 스펙 계산 대상 여부
	 */
	public boolean isManualSpecCalculationTarget() {
		return isSpecAvailable && !isAutoSpec && isNumberType();
	}

	/**
	 * 자동 스펙 계산 대상 여부
	 */
	public boolean isAutoSpecCalculationTarget() {
		return (isSpecAvailable && isAutoSpec) && ((autoCalcCurrCnt == null || autoCalcCurrCnt == 0) || (autoCalcCurrCnt > 0 && isReqRecalculate));
	}

	/**
	 * 가상 파라미터 계산에 필요한 일반 파라미터 목록
	 */
	public List<Parameter> getCalculationRequiredParameters() {
		if (!isVirtual) {
			return List.of();
		}
		return mappingParameters.stream()
								.filter(VirtualParameterParameterMapping::isUsingCalculation)
								.map(VirtualParameterParameterMapping::getParameter)
								.toList();
	}

	public boolean isMetaParameter() {
		return this.type == Type.META_DATA;
	}

	/**
	 * 파라미터 타입이 META_DATA인 파라미터의 메타데이터 값 업데이트
	 */
	public void modifyMetaParameterValue(String value) {
		this.metaValue = value;
	}
}
