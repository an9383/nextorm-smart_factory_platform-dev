package com.nextorm.common.db.entity.system.code;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "code_hierarchy")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class CodeHierarchy extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Code parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_id")
	private Code child;

	public static CodeHierarchy create(
		Code parent,
		Code child
	) {
		return CodeHierarchy.builder()
							.parent(parent)
							.child(child)
							.build();
	}
}
