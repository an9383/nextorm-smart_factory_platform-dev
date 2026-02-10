package com.nextorm.common.apc.repository;

import com.nextorm.common.apc.dto.ApcRequestSearchDto;
import com.nextorm.common.apc.dto.ApcRequestStatusDto;
import com.nextorm.common.apc.entity.ApcRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface ApcRequestQueryDsl {
	List<ApcRequest> findBySearchParam(
		ApcRequestSearchDto apcRequestSearchDto
	);

	List<ApcRequestStatusDto> findApcRequestStatusList(
		LocalDateTime from,
		LocalDateTime to,
		ApcRequest.Type type
	);
}
