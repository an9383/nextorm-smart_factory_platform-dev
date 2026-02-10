package com.nextorm.apcmodeling.dto;

import com.nextorm.common.apc.constant.ApcErrorCode;
import com.nextorm.common.apc.dto.ApcRequestStatusDto;
import com.nextorm.common.apc.entity.ApcRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApcRequestStatusResponseDto {
	private Long id;
	private ApcRequest.Status status;
	private String requestDataJson;
	private LocalDateTime createAt;
	private ApcErrorCode errorCode;
	private String modelCondition;
	private String modelName;
	private String formula;
	private Integer modelVersion;
	private Map<String, String> requestMap;

	public static ApcRequestStatusResponseDto from(
		ApcRequestStatusDto apcRequestStatusDto,
		Map<String, String> requestMap
	) {
		return ApcRequestStatusResponseDto.builder()
										  .id(apcRequestStatusDto.getId())
										  .status(apcRequestStatusDto.getStatus())
										  .requestDataJson(apcRequestStatusDto.getRequestDataJson())
										  .createAt(apcRequestStatusDto.getCreateAt())
										  .errorCode(apcRequestStatusDto.getErrorCode())
										  .modelCondition(apcRequestStatusDto.getModelCondition())
										  .modelName(apcRequestStatusDto.getModelName())
										  .formula(apcRequestStatusDto.getFormula())
										  .modelVersion(apcRequestStatusDto.getVersion())
										  .requestMap(requestMap)
										  .build();
	}

}
