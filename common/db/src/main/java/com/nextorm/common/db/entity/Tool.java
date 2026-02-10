package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "tool")
@Getter
public class Tool extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Location location;

	public enum Type {
		PROCESS, MEASURE
	}

	public enum ToolType {
		MAIN, SUB
	}

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", columnDefinition = "varchar(20)")
	private Type type;

	@Enumerated(EnumType.STRING)
	@Column(name = "tool_type", columnDefinition = "varchar(20)")
	private ToolType toolType;

	@Column(name = "parent_id")
	private Long parentId;

	@OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Parameter> parameters;

	public void modify(
		Tool tool,
		Location location
	) {
		if (Objects.nonNull(tool)) {
			this.location = location;
			this.name = tool.getName();
			this.toolType = tool.getToolType();
			this.type = tool.getType();
		}
	}
}
