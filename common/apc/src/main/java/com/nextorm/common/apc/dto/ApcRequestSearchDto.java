package com.nextorm.common.apc.dto;

import com.nextorm.common.apc.entity.ApcRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApcRequestSearchDto {
	private Long versionId;
	private LocalDateTime from;
	private LocalDateTime to;
	private ApcRequest.Type type;
	private ApcRequest.Status status;
}
