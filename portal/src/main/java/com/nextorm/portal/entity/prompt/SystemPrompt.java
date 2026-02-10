package com.nextorm.portal.entity.prompt;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "system_propmt")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemPrompt extends BaseEntity {

	@Comment("시스템 메세지")
	@Column(name = "message", nullable = false)
	private String message;

}
