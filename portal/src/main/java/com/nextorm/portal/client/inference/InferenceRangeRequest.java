package com.nextorm.portal.client.inference;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class InferenceRangeRequest {
	private String site;
	private Long modelId;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalDateTime fromDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalDateTime toDate;
}
