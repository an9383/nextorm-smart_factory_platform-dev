package com.nextorm.common.db.entity;

import com.nextorm.common.db.BooleanToStringConverter;
import com.nextorm.common.db.MapToJsonStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dcp_config")
@Getter
public class DcpConfig extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@OneToMany(mappedBy = "dcpConfig", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,
		CascadeType.PERSIST}, orphanRemoval = true)
	@Comment("DcpConfig와 Parameter의 매핑 정보")
	@Builder.Default
	private List<DcpConfigParameterMapping> parameterMappings = new ArrayList<>();

	@Column(name = "topic")
	@Comment("카프카 토픽")
	private String topic;

	@OneToMany(mappedBy = "dcpConfig", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,
		CascadeType.PERSIST}, orphanRemoval = true)
	@Comment("DcpConfig와 Rule의 매핑 정보")
	@Builder.Default
	private List<DcpConfigRuleMapping> ruleMappings = new ArrayList<>();

	@Column(name = "bootstrap_server")
	@Comment("카프카 연결 정보")
	private String bootstrapServer;

	@Column(name = "command")
	private String command;

	@Column(name = "data_interval")
	@Comment("데이터 수집 주기")
	private Integer dataInterval;

	@Column(name = "is_use", length = 1)
	@Convert(converter = BooleanToStringConverter.class)
	private Boolean isUse = true;

	@Column(name = "collector_type")
	@Comment("데이터 컬렉터 타입 (collector_defines 테이블의 type값이 들어감)")
	private String collectorType;

	@Column(name = "last_collected_at")
	@Comment("Dcp가 마지막으로 데이터를 수집하여 저장한 시간")
	private LocalDateTime lastCollectedAt;

	@Column(name = "collector_arguments", length = 1000)
	@Convert(converter = MapToJsonStringConverter.class)
	@Comment("Collector에 전달할 인자 정보")
	private Map<String, Object> collectorArguments;

	@Column(name = "is_geo_data_type")
	@Convert(converter = BooleanToStringConverter.class)
	@Comment("지리 데이터 타입에 대한 사용 여부")
	private boolean isGeoDataType;

	@Column(name = "latitude_parameter_name")
	@Comment("위도 파라미터 데이터명")
	private String latitudeParameterName;

	@Column(name = "longitude_parameter_name")
	@Comment("경도 파라미터 데이터명")
	private String longitudeParameterName;

	public static DcpConfig create(
		DcpConfig createData,
		Tool tool,
		List<Parameter> parameters,
		List<Rule> rules
	) {
		DcpConfig entity = DcpConfig.builder()
									.topic(createData.topic)
									.bootstrapServer(createData.bootstrapServer)
									.command(createData.command)
									.dataInterval(createData.dataInterval)
									.isUse(true)
									.tool(tool)
									.collectorType(createData.getCollectorType())
									.lastCollectedAt(createData.lastCollectedAt)
									.collectorArguments(createData.collectorArguments)
									.isGeoDataType(createData.isGeoDataType)
									.latitudeParameterName(createData.latitudeParameterName)
									.longitudeParameterName(createData.longitudeParameterName)
									.build();
		parameters.forEach(entity::addParameter);
		rules.forEach(entity::addRule);
		return entity;
	}

	public List<Parameter> getParameters() {
		return this.parameterMappings.stream()
									 .map(DcpConfigParameterMapping::getParameter)
									 .toList();
	}

	public void addParameter(Parameter parameter) {
		this.parameterMappings.add(DcpConfigParameterMapping.create(this, parameter));
	}

	public void removeParameter(Parameter parameter) {
		this.parameterMappings.stream()
							  .filter(mapping -> mapping.getParameter()
														.getId()
														.equals(parameter.getId()))
							  .findFirst()
							  .ifPresent(mapping -> this.parameterMappings.remove(mapping));
	}

	public void addRule(Rule rule) {
		this.ruleMappings.add(DcpConfigRuleMapping.create(this, rule));
	}

	public List<Rule> getRules() {
		return this.ruleMappings.stream()
								.map(DcpConfigRuleMapping::getRule)
								.toList();
	}

	public void modify(
		DcpConfig modifyData,
		List<Parameter> parameters,
		List<Rule> rules
	) {
		if (Objects.nonNull(modifyData)) {
			this.parameterMappings.clear();
			parameters.forEach(this::addParameter);

			this.ruleMappings.clear();
			rules.forEach(this::addRule);

			this.bootstrapServer = modifyData.getBootstrapServer();
			this.command = modifyData.getCommand();
			this.dataInterval = modifyData.getDataInterval();
			this.collectorType = modifyData.getCollectorType();
			this.lastCollectedAt = modifyData.getLastCollectedAt();
			this.collectorArguments = modifyData.getCollectorArguments();
			this.isGeoDataType = modifyData.isGeoDataType();
			this.latitudeParameterName = modifyData.getLatitudeParameterName();
			this.longitudeParameterName = modifyData.getLongitudeParameterName();
		}
	}

	public void modifyLastCollectedAt() {
		this.lastCollectedAt = LocalDateTime.now();
	}
}
