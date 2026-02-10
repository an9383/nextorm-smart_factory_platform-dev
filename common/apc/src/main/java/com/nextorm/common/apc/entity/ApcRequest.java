package com.nextorm.common.apc.entity;

import com.nextorm.common.apc.constant.ApcErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "apc_request")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcRequest extends BaseEntity {
	@Column(name = "apc_model_version_id")
	private Long apcModelVersionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", columnDefinition = "varchar(20)", nullable = false)
	private Type type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "varchar(20)", nullable = false)
	private Status status;

	@Column(name = "request_data_json", columnDefinition = "TEXT")
	private String requestDataJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "error_code", columnDefinition = "varchar(64)")
	private ApcErrorCode errorCode;

	public static ApcRequest createSimulationInit(
		Long apcModelVersionId,
		String dataJsonString
	) {
		return initBuilderBase(apcModelVersionId, dataJsonString).type(Type.SIMULATION)
																 .build();
	}

	public static ApcRequest createProcessInit(
		Long apcModelVersionId,
		String dataJsonString
	) {
		return initBuilderBase(apcModelVersionId, dataJsonString).type(Type.NORMAL)
																 .build();
	}

	public static ApcRequest createSimulationModelNotFound(String dataJsonString) {
		return ApcRequest.builder()
						 .type(Type.SIMULATION)
						 .status(Status.NOT_FOUND)
						 .requestDataJson(dataJsonString)
						 .build();

	}

	public static ApcRequest createProcessModelNotFound(String dataJsonString) {
		return ApcRequest.builder()
						 .type(Type.NORMAL)
						 .status(Status.NOT_FOUND)
						 .requestDataJson(dataJsonString)
						 .build();

	}

	private static ApcRequestBuilder initBuilderBase(
		Long apcModelVersionId,
		String dataJsonString
	) {
		return ApcRequest.builder()
						 .apcModelVersionId(apcModelVersionId)
						 .requestDataJson(dataJsonString)
						 .status(Status.RUNNING);
	}

	public static ApcRequest createErrorRequest(
		Type requestType,
		ApcErrorCode errorCode
	) {
		return ApcRequest.builder()
						 .type(requestType)
						 .status(Status.ERROR)
						 .errorCode(errorCode)
						 .build();
	}

	public boolean isSimulation() {
		return this.type == Type.SIMULATION;
	}

	public boolean isRunning() {
		return this.status == Status.RUNNING;
	}

	public void updateSuccess() {
		this.status = Status.SUCCESS;
	}

	public void updateError(ApcErrorCode errorCode) {
		this.status = Status.ERROR;
		this.errorCode = errorCode;
	}

	public boolean isSuccess() {
		return this.status == Status.SUCCESS;
	}

	public enum Type {
		NORMAL, SIMULATION
	}

	public enum Status {
		RUNNING, SUCCESS, ERROR, NOT_FOUND
	}
}
