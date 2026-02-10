package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservoir_last")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ReservoirLast extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;
	@Column(name = "longitude")
	private Double longitude;
	@Column(name = "latitude")
	private Double latitude;
	@Column(name = "depth")
	private Double depth;
	@Column(name = "ph_units")
	private Double phUnits;
	@Column(name = "spcond_us_cm")
	private Double spcondUsCm;
	@Column(name = "turb_ntu")
	private Double turbNtu;
	@Column(name = "bg_ppb")
	private Double bgPpb;
	@Column(name = "chl_ug_l")
	private Double chlUgL;
	@Column(name = "hd0_mg_l")
	private Double hd0MgL;
	@Column(name = "hd0_sat")
	private Double hd0Sat;
	@Column(name = "ph_mv")
	private Double phMv;
	@Column(name = "create_date")
	private LocalDateTime createDate;

}
