package com.nextorm.portal.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseTreeItem {
	public interface ParentFinder {
		BaseTreeItem findParent(
			List<BaseTreeItem> items,
			BaseTreeItem currentItem
		);
	}

	public interface TreeIdGenerator {
		String generate(BaseTreeItem item);
	}

	private final Long id;
	private final String name;
	private final Long parent;
	private String type;
	private String parentType;

	private List<BaseTreeItem> children;
	private Object object;
	@JsonIgnore
	private ParentFinder parentFinder;
	@JsonIgnore
	private TreeIdGenerator treeIdGenerator;

	public BaseTreeItem(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.type = builder.type;
		this.parent = builder.parent;
		this.parentType = builder.parentType;
		this.children = builder.children;
		this.object = builder.object;
		this.parentFinder = builder.parentFinder;
		this.treeIdGenerator = builder.treeIdGenerator;
	}

	public static class Builder {
		private final Long id;
		private final String name;
		private final Long parent;
		private String type;
		private String parentType;

		private List<BaseTreeItem> children = new ArrayList<BaseTreeItem>();
		private Object object;
		private final ParentFinder parentFinder;
		private TreeIdGenerator treeIdGenerator;

		public Builder(
			Long id,
			String name,
			Long parent
		) {
			this.id = id;
			this.name = name;
			this.parent = parent;
			this.parentFinder = null;
		}

		public Builder(
			Long id,
			String name,
			Long parent,
			ParentFinder parentFinder
		) {
			this.id = id;
			this.name = name;
			this.parent = parent;
			this.parentFinder = parentFinder;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder parentType(String parentType) {
			this.parentType = parentType;
			return this;
		}

		public Builder children(List<BaseTreeItem> children) {
			this.children = children;
			return this;
		}

		public Builder object(Object object) {
			this.object = object;
			return this;
		}

		public Builder treeIdGenerator(TreeIdGenerator treeIdGenerator) {
			this.treeIdGenerator = treeIdGenerator;
			return this;
		}

		public BaseTreeItem build() {
			return new BaseTreeItem(this);
		}
	}

	public String getTreeId() {
		if (treeIdGenerator == null) {
			return type != null
				   ? type + "_" + id
				   : String.valueOf(id);
		} else {
			return treeIdGenerator.generate(this);
		}
	}

	public String getParentTree() {
		return parentType != null
			   ? parentType + "_" + parent
			   : (parent != null
				  ? String.valueOf(parent)
				  : null);
	}
}
