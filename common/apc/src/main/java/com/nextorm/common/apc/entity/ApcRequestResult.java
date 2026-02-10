package com.nextorm.common.apc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "apc_request_result")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcRequestResult extends BaseEntity {
	@Column(name = "apc_request_id")
	private Long apcRequestId;

	@Column(name = "result_key")
	private String resultKey;

	@Column(name = "result_value")
	private String resultValue;

	public static ApcRequestResult create(
		Long apcRequestId,
		String resultKey,
		String resultValue
	) {
		return ApcRequestResult.builder()
							   .apcRequestId(apcRequestId)
							   .resultKey(resultKey)
							   .resultValue(resultValue)
							   .build();
	}

	@Override
	public String toString() {
		return "ApcRequestResult{id=" + id + ", apcRequestId=" + apcRequestId + ", resultKey='" + resultKey + '\'' + ", resultValue='" + resultValue + "'}";
	}
}
