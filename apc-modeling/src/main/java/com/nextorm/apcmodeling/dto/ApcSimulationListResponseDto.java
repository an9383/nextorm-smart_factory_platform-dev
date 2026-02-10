package com.nextorm.apcmodeling.dto;

import com.nextorm.common.apc.entity.ApcModelSimulation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApcSimulationListResponseDto {
	private Long apcSimulationId;

	private String condition;

	private LocalDateTime createAt;

	private String formula;

	private int version;

	private ApcModelSimulation.Status status;

	private List<ApcSimulationDataDto> apcSimulationDatas;

	private String createBy;
}
