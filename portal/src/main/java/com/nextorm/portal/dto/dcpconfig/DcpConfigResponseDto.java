package com.nextorm.portal.dto.dcpconfig;

import com.nextorm.common.db.entity.DcpConfig;
import com.nextorm.portal.dto.parameter.ParameterResponseDto;
import com.nextorm.portal.dto.rule.RuleResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DcpConfigResponseDto {
	private Long id;
	private Long toolId;
	private String toolName;

	private String command;

	private Integer dataInterval;

	private List<ParameterResponseDto> parameters;
	private List<RuleResponseDto> rules;

	private String collectorType;
	private Map<String, Object> collectorArguments;

	private Boolean isGeoDataType;
	private String latitudeParameterName;
	private String longitudeParameterName;

	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static DcpConfigResponseDto from(DcpConfig entity) {
		if (entity == null) {
			return null;
		}

		return DcpConfigResponseDto.builder()
								   .id(entity.getId())
								   .command(entity.getCommand())
								   .dataInterval(entity.getDataInterval())
								   .toolId(entity.getTool()
												 .getId())
								   .toolName(entity.getTool()
												   .getName())
								   .parameters(entity.getParameters()
													 .stream()
													 .map(ParameterResponseDto::from)
													 .toList())
								   .rules(entity.getRules()
												.stream()
												.map(RuleResponseDto::from)
												.toList())
								   .collectorType(entity.getCollectorType())
								   .collectorArguments(entity.getCollectorArguments())
								   .isGeoDataType(entity.isGeoDataType())
								   .latitudeParameterName(entity.getLatitudeParameterName())
								   .longitudeParameterName(entity.getLongitudeParameterName())
								   .createBy(entity.getCreateBy())
								   .updateAt(entity.getUpdateAt())
								   .updateBy(entity.getUpdateBy())
								   .build();
	}

}
