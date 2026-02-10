package com.nextorm.common.db.entity.system.code;

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
@Table(name = "code_category")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CodeCategory extends BaseEntity {

	@Column(unique = true)
	@Comment("코드 카테고리")
	private String category;

	@Comment("카테고리명")
	private String name;

	@Comment("설명")
	private String description;

	public void modify(
		String name,
		String description
	) {
		this.name = name;
		this.description = description;
	}

}
