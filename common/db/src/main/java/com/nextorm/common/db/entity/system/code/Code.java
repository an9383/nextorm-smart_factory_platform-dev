package com.nextorm.common.db.entity.system.code;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "`code`", uniqueConstraints = @UniqueConstraint(columnNames = {"category", "code"}))
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Code extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category", referencedColumnName = "category", nullable = false)
	@Comment("코드 카테고리")
	private CodeCategory category;

	@Comment("코드")
	@Column(name = "`code`")
	private String code;

	@Comment("이름")
	private String name;

	@Comment("코드값")
	@Column(name = "`value`")
	private String value;

	@Comment("정렬순서")
	private Integer sort;

	@Comment("설명")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_category", referencedColumnName = "category")
	@Comment("하위 카테고리")
	private CodeCategory childCategory;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE,
		CascadeType.PERSIST}, orphanRemoval = true)
	@Builder.Default
	private Set<CodeHierarchy> childCodes = new HashSet<>();

	@OneToMany(mappedBy = "child", fetch = FetchType.LAZY, orphanRemoval = true)
	@Builder.Default
	private Set<CodeHierarchy> parentCodes = new HashSet<>();

	public void clearChildCode() {
		this.childCodes.clear();
	}

	public void addChildCode(Code childCode) {
		this.childCodes.add(CodeHierarchy.create(this, childCode));
	}

	public void modify(
		String name,
		String value,
		String description,
		CodeCategory childCategory,
		List<Code> childCodes
	) {
		this.name = name;
		this.childCategory = childCategory;
		this.value = value;
		this.description = description;
		this.clearChildCode();
		childCodes.forEach(this::addChildCode);
	}

	public void modifySort(int sort) {
		this.sort = sort;
	}

}
