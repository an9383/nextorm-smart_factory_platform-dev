package com.nextorm.portal.entity.system;

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
@Table(name = "role")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String name;

	@Comment("설명")
	private String description;

	@Column(length = 10240, name = "permission")
	@Comment("권한 정보(JSON)")
	private String permission;

	public void modify(
		String name,
		String description,
		String permission
	) {
		this.name = name;
		this.description = description;
		this.permission = permission;
	}

}
