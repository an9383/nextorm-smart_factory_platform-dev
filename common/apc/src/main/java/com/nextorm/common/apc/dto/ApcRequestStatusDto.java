package com.nextorm.common.apc.dto;

import com.nextorm.common.apc.constant.ApcErrorCode;
import com.nextorm.common.apc.entity.ApcRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ApcRequestStatusDto {
	private Long id;
	private ApcRequest.Status status;
	private String requestDataJson;
	private LocalDateTime createAt;
	private ApcErrorCode errorCode;
	private String modelCondition;
	private String modelName;
	private String formula;
	private Integer version;

}
