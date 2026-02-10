package com.nextorm.portal.dto.aimodel.correlation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CorrelationRequest {

	@JsonProperty("yParameterId")
	private Long yParameterId;
	@JsonProperty("xParameterIds")
	private List<Long> xParameterIds;
	private LocalDateTime from;
	private LocalDateTime to;

}
