package com.nextorm.portal.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.CollectorDefine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CollectorTypeMetaResponseDto {
	private String type;
	private String displayName;
	private List<Argument> arguments;

	public static CollectorTypeMetaResponseDto of(CollectorDefine define) {
		List<Argument> arguments = define.getArguments()
										 .stream()
										 .map(Argument::new)
										 .toList();
		return new CollectorTypeMetaResponseDto(define.getType(), define.getDisplayName(), arguments);
	}

	@Getter
	public static class Argument {
		private String key;
		private String type;
		@JsonProperty("required")
		private boolean required;
		private List<ExtraDataDefine> extraDataDefines = List.of();

		public Argument(Map<String, Object> argument) {
			this.key = argument.get("key")
							   .toString();

			this.type = argument.get("type")
								.toString();

			this.required = "true".equalsIgnoreCase(argument.getOrDefault("required", "true")
															.toString());

			if (argument.containsKey("extraDataDefines")) {
				List<Map<String, Object>> extraDataDefines = (List<Map<String, Object>>)argument.getOrDefault(
					"extraDataDefines",
					List.of());

				this.extraDataDefines = extraDataDefines.stream()
														.map(defineMap -> {
															boolean required = "true".equalsIgnoreCase(defineMap.getOrDefault(
																													"required",
																													"true")
																												.toString());
															return new ExtraDataDefine(defineMap.get("key")
																								.toString(),
																defineMap.get("dataType")
																		 .toString(),
																required);
														})
														.toList();
			}
		}

		@Getter
		@AllArgsConstructor
		class ExtraDataDefine {
			private String key;
			private String dataType;
			@JsonProperty("required")
			private boolean required;
		}
	}
}
