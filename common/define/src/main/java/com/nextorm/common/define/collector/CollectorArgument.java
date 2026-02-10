package com.nextorm.common.define.collector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectorArgument {
	private String key;
	private ArgumentType type;
	private boolean required;
	private List<ExtraDataDefine> extraDataDefines;

	public static CollectorArgument of(
		String key,
		ArgumentType type
	) {
		return new CollectorArgument(key, type, true, List.of());
	}

	public static CollectorArgument of(
		String key,
		ArgumentType type,
		boolean required
	) {
		return new CollectorArgument(key, type, required, List.of());
	}

	public static CollectorArgument of(
		String key,
		ArgumentType type,
		List<ExtraDataDefine> extraDataDefines
	) {
		return new CollectorArgument(key, type, true, extraDataDefines);
	}

	@Getter
	@AllArgsConstructor
	public static class ExtraDataDefine {
		private String key;
		private DataType dataType;
		private boolean required;

		public ExtraDataDefine(
			String key,
			DataType dataType
		) {
			this.key = key;
			this.dataType = dataType;
			this.required = true;
		}

		public enum DataType {
			STRING, NUMBER
		}
	}
}
