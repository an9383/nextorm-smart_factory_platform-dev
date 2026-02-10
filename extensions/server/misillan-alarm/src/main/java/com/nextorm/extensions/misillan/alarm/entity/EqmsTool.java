package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * EQMS의 설비 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "eqms_tools")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EqmsTool {
	@Id
	@Comment("eqms 설비 ID")
	private Long id;

	@Comment("설비명")
	@Column(name = "name")
	private String name;

	@Comment("마지막 수정 시간")
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void modify(EqmsTool tool) {
		this.name = tool.getName();
		this.updatedAt = tool.getUpdatedAt();
	}
}
