package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * SFP의 파라미터 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "sfp_parameters")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SfpParameter {
	public enum DataType {
		INTEGER, DOUBLE
	}

	@Id
	@Comment("SFP 파라미터 ID")
	private Long id;

	@Comment("파라미터명")
	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Comment("데이터 타입")
	@Column(name = "data_type")
	private DataType dataType;

	@Comment("마지막 수정 시간")
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void modify(SfpParameter parameter) {
		this.name = parameter.getName();
		this.dataType = parameter.getDataType();
		this.updatedAt = parameter.getUpdatedAt();
	}
}
