package com.nextorm.common.db.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class ModelBuildRequestDto {

	private String name;
	private String description;
	private String algorithm;
	private LocalDateTime from;
	private LocalDateTime to;

	@JsonProperty("yParameterId")
	private Long yParameterId;
	@JsonProperty("xParameters")
	private List<XParameter> xParameters;
}
