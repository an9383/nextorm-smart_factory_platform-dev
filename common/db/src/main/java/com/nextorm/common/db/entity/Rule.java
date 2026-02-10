package com.nextorm.common.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "rule")
// @Data
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Rule extends BaseEntity {
	@Column(name = "name")
	private String name;
	@Column(name = "class_name")
	private String className;
	@Column(name = "description")
	private String description;

	//    @ManyToOne(fetch = FetchType.LAZY)
	//    @JoinColumn(name="dcp_config_id")
	//    private DcpConfig dcpConfig;
	public void modify(Rule rule) {
		this.name = rule.getName();
		this.className = rule.getClassName();
		this.description = rule.getDescription();
	}
}
