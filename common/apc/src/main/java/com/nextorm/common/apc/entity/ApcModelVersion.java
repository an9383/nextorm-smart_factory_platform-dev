package com.nextorm.common.apc.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "apc_model_version")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class ApcModelVersion extends BaseEntity {
	@Comment("APC Model ID")
	@JoinColumn(name = "apc_model_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private ApcModel apcModel;

	@Comment("모델 버전")
	@Column(name = "version", nullable = false)
	private Integer version;

	@Comment("수식 스크립트 (js)")
	@Column(name = "formula_script", columnDefinition = "TEXT", nullable = false)
	private String formulaScript;

	@Comment("수식 생성 시 사용된 워크스페이스 정보 (json)")
	@Column(name = "formula_workspace", columnDefinition = "TEXT", nullable = false)
	private String formulaWorkspace;

	@Comment("설명")
	@Column(name = "description", length = 500)
	private String description;

	@Comment("APC 실행 결과 알림을 수행할지 여부")
	@Column(name = "is_use_notify", nullable = false)
	private boolean isUseNotify;

	@Comment("Active 여부")
	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Comment("삭제 여부")
	@Column(name = "is_delete", nullable = false)
	private boolean isDelete;

	public void modifyApcModelVersion(
		ApcModelVersion apcModelVersion
	) {
		this.description = apcModelVersion.getDescription();
		this.formulaScript = apcModelVersion.getFormulaScript();
		this.formulaWorkspace = apcModelVersion.getFormulaWorkspace();
		this.isActive = apcModelVersion.isActive;
		this.isUseNotify = apcModelVersion.isUseNotify;
	}

	public void inActiveApcModelVersion() {
		this.isActive = false;
	}
}
