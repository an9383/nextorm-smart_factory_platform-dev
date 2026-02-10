package com.nextorm.common.apc.entity;

import com.nextorm.common.apc.constant.ApcErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "apc_model_simulation_data")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcModelSimulationData extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "apc_model_simulation_id", nullable = false)
	private ApcModelSimulation apcModelSimulation;

	@Column(name = "apc_request_id")
	private Long apcRequestId;

	@Column(name = "data_json", columnDefinition = "TEXT")
	private String dataJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "error_code", columnDefinition = "varchar(64)")
	private ApcErrorCode errorCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
	private Status status;

	public enum Status {
		WAITING, SENT, FAIL, COMPLETE, CANCEL
	}

	@Column(name = "trace_at")
	private LocalDateTime traceAt;

	public void setStatus(Status status) {
		this.status = status;
	}

	public void initRequestId(Long requestId) {
		this.apcRequestId = requestId;
	}

	public void setErrorCode(ApcErrorCode errorCode) {
		this.errorCode = errorCode;
	}

}
