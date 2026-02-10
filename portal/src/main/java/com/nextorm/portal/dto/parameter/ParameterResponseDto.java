package com.nextorm.portal.dto.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterResponseDto {
	private Long id;
	private String name;
	private Parameter.Type type;
	private Parameter.DataType dataType;

	private Long toolId;
	private String toolName;
	private String unit;
	private String metaValue;

	@JsonProperty("isVirtual")
	private boolean isVirtual;
	private Boolean isSpecAvailable;
	private Boolean isAutoSpec;
	private Boolean isReqRecalculate;
	private Integer autoCalcCurrCnt;
	private Integer autoCalcPeriod;

	private Double target;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;

	private Integer order;

	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ParameterResponseDto from(Parameter entity) {
		if (entity == null) {
			return null;
		}

		Tool tool = entity.getTool();

		return ParameterResponseDto.builder()
								   .toolId(tool != null
										   ? tool.getId()
										   : null)
								   .toolName(tool != null
											 ? tool.getName()
											 : null)

								   .id(entity.getId())
								   .name(entity.getName())
								   .type(entity.getType())
								   .unit(entity.getUnit())
								   .dataType(entity.getDataType())
								   .isVirtual(entity.isVirtual())
								   .isSpecAvailable(entity.isSpecAvailable())
								   .isAutoSpec(entity.isAutoSpec())
								   .isReqRecalculate(entity.isReqRecalculate())
								   .autoCalcCurrCnt(entity.getAutoCalcCurrCnt())
								   .autoCalcPeriod(entity.getAutoCalcPeriod())
								   .target(entity.getTarget())
								   .ucl(entity.getUcl())
								   .lcl(entity.getLcl())
								   .usl(entity.getUsl())
								   .lsl(entity.getLsl())
								   .order(entity.getOrder())
								   .createAt(entity.getCreateAt())
								   .createBy(entity.getCreateBy())
								   .updateAt(entity.getUpdateAt())
								   .updateBy(entity.getUpdateBy())
								   .metaValue(entity.getMetaValue())
								   .build();
	}
}
