package com.nextorm.portal.dto;

import com.nextorm.common.db.entity.ReservoirLast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservoirLastResponseDto {

	private Long toolId;

	private Double longitude;
	private Double latitude;

	private Double depth;

	private Double phUnits;
	private Double spcondUsCm;
	private Double turbNtu;
	private Double bgPpb;
	private Double chlUgL;
	private Double hd0MgL;
	private Double hd0Sat;
	private Double phMv;

	private LocalDateTime createDate;
	//BaseDto
	private Long id;
	private String createBy;
	private LocalDateTime createAt;
	private String updateBy;
	private LocalDateTime updateAt;

	public static ReservoirLastResponseDto from(ReservoirLast entity) {
		if (entity == null) {
			return null;
		}
		return ReservoirLastResponseDto.builder()
									   .toolId(entity.getTool()
													 .getId() != null
											   ? entity.getTool()
													   .getId()
											   : null)
									   .longitude(entity.getLongitude())
									   .latitude(entity.getLatitude())
									   .depth(entity.getDepth())
									   .phUnits(entity.getPhUnits())
									   .spcondUsCm(entity.getSpcondUsCm())
									   .turbNtu(entity.getTurbNtu())
									   .bgPpb(entity.getBgPpb())
									   .chlUgL(entity.getChlUgL())
									   .hd0MgL(entity.getHd0MgL())
									   .hd0Sat(entity.getHd0Sat())
									   .phMv(entity.getPhMv())
									   .createDate(entity.getCreateDate())
									   .id(entity.getId())
									   .createAt(entity.getCreateAt())
									   .createBy(entity.getCreateBy())
									   .updateAt(entity.getUpdateAt())
									   .updateBy(entity.getUpdateBy())
									   .build();
	}
}
