package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * EQMS의 품목 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "eqms_products")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EqmsProduct {
	@Id
	@Comment("eqms 품목 ID")
	private Long id;

	@Comment("품목명")
	@Column(name = "name")
	private String name;

	@Comment("마지막 수정 시간")
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void modify(EqmsProduct product) {
		this.name = product.getName();
		this.updatedAt = product.getUpdatedAt();
	}
}
