package com.nextorm.common.define.collector;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectorTypeDefineRequestDto {
	private int version;
	private List<CollectorTypeDefine> collectorTypes = new ArrayList<>();

	@Data
	public static class CollectorTypeDefine {
		private String type;
		private String displayName;
		private List<Argument> arguments;
	}

	@Data
	public static class Argument {
		private String key;
		private ArgumentType type;
	}
}
