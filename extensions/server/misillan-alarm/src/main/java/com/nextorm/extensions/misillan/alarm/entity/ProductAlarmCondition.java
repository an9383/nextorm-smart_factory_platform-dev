package com.nextorm.extensions.misillan.alarm.entity;

import com.nextorm.extensions.misillan.alarm.dto.productalarmcondition.ProductAlarmConditionModifyDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * 품목별 알람 조건 정보를 저장하는 엔티티
 * <p>
 * - 각 품목에 대해 설비별 알람조건 설정시 기본값으로 사용할 온도, 압력 등의 알람 조건값을 저장
 */
@Entity
@Table(name = "product_alarm_conditions")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductAlarmCondition {
	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "eqms_product_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private EqmsProduct product;

	@Comment("온도")
	@Column(name = "temperature")
	private Double temperature;

	@Comment("압력")
	@Column(name = "pressure")
	private Double pressure;

	public void modify(EqmsProduct eqmsProduct, ProductAlarmConditionModifyDto productAlarmConditionModifyDto){
		this.product = eqmsProduct;
		this.temperature = productAlarmConditionModifyDto.getTemperature();
		this.pressure = productAlarmConditionModifyDto.getPressure();
	}


}
