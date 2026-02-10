package com.nextorm.common.apc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "apc_model")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcModel extends BaseEntity {
	@Comment("모델 실행 조건 (구분자: |;)")
	@Column(name = "`condition`", nullable = false)
	private String condition;

	@Comment("사용 여부")
	@Column(name = "is_use", nullable = false)
	private boolean isUse;

	@Comment("삭제 여부")
	@Column(name = "is_delete", nullable = false)
	private boolean isDelete;

	@Comment("모델명")
	@Column(name = "model_name", nullable = false)
	private String modelName;

	public void modifyApcModel(
		String modelName,
		boolean isUse
	) {
		this.modelName = modelName;
		this.isUse = isUse;
	}

	public void deleteApcModel() {
		this.isDelete = true;
	}
}
