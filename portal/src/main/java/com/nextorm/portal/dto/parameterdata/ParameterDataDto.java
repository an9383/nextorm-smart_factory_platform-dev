package com.nextorm.portal.dto.parameterdata;

import com.nextorm.common.db.entity.ParameterData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDataDto {
	private Long parameterId;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;

	private Double dValue;
	private String sValue;
	private Integer iValue;
	private String imageValue;
	private Double latitudeValue;
	private Double longitudeValue;
	private LocalDateTime traceAt;
	private Boolean isCtrlLimitOver;
	private Boolean isSpecLimitOver;

	//BaseDto
	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ParameterDataDto from(ParameterData entity) {
		if (entity == null) {
			return null;
		}
		return ParameterDataDto.builder()
							   .parameterId(entity.getParameterId())
							   .ucl(entity.getUcl())
							   .lcl(entity.getLcl())
							   .usl(entity.getUsl())
							   .lsl(entity.getLsl())
							   .dValue(entity.getDValue())
							   .sValue(entity.getSValue())
							   .iValue(entity.getIValue())
							   .imageValue(entity.getImageValue())
							   .latitudeValue(entity.getLatitudeValue())
							   .longitudeValue(entity.getLongitudeValue())
							   .traceAt(entity.getTraceAt())
							   .id(entity.getId())
							   .createAt(entity.getCreateAt())
							   .createBy(entity.getCreateBy())
							   .updateAt(entity.getUpdateAt())
							   .isCtrlLimitOver(entity.isCtrlLimitOver())
							   .isSpecLimitOver(entity.isSpecLimitOver())
							   .updateBy(entity.getUpdateBy())
							   .build();
	}

	public ParameterData toEntity() {
		return ParameterData.builder()
							.parameterId(parameterId != null
										 ? parameterId
										 : null)
							.ucl(ucl)
							.lcl(lcl)
							.usl(usl)
							.lsl(lsl)
							.dValue(dValue)
							.sValue(sValue)
							.iValue(iValue)
							.imageValue(imageValue)
							.latitudeValue(latitudeValue)
							.longitudeValue(longitudeValue)
							.traceAt(traceAt)
							.isCtrlLimitOver(isCtrlLimitOver)
							.isSpecLimitOver(isSpecLimitOver)
							.createAt(createAt)
							.createBy(createBy)
							.updateBy(updateBy)
							.updateAt(updateAt)
							.build();
	}
}
