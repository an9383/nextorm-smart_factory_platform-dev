package com.nextorm.apcmodeling.service.interfaces;

import com.nextorm.apcmodeling.dto.ApcTargetDataDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ApcTargetDataService {
	List<ApcTargetDataDto> getApcTargetData(
		LocalDateTime from,
		LocalDateTime to
	);
}
