package com.nextorm.apcmodeling.dto;

import com.nextorm.common.apc.entity.ApcRequestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApcRequestResultDto {
	private Long id;
	private Long apcRequestId;
	private String resultKey;
	private String resultValue;

	public static ApcRequestResultDto from(ApcRequestResult apcRequestResult) {
		return ApcRequestResultDto.builder()
								  .id(apcRequestResult.getId())
								  .apcRequestId(apcRequestResult.getApcRequestId())
								  .resultKey(apcRequestResult.getResultKey())
								  .resultValue(apcRequestResult.getResultValue())
								  .build();
	}
}
