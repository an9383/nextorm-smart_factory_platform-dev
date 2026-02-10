package com.nextorm.common.apc.entity;

import com.nextorm.common.apc.constant.ApcErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apc_model_simulation")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcModelSimulation extends BaseEntity {

	@JoinColumn(name = "apc_model_version_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private ApcModelVersion apcModelVersion;

	@OneToMany(mappedBy = "apcModelSimulation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ApcModelSimulationData> apcModelSimulationDataList = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
	private Status status;

	public enum Status {
		RUNNING, COMPLETE, CANCEL
	}

	public void addSimulationData(
		String dataJson,
		ApcErrorCode errorCode,
		ApcModelSimulationData.Status status,
		LocalDateTime traceAt
	) {
		ApcModelSimulationData.ApcModelSimulationDataBuilder builder = ApcModelSimulationData.builder()
																							 .apcModelSimulation(this)
																							 .dataJson(dataJson)
																							 .errorCode(errorCode)
																							 .status(status)
																							 .traceAt(traceAt);
		this.apcModelSimulationDataList.add(builder.build());
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
