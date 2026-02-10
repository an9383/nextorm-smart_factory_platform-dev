package com.nextorm.common.db.entity;

import com.nextorm.common.db.ListMapToJsonStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "collector_defines")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class CollectorDefine extends BaseEntity {
	@Column(name = "type")
	@Comment("컬렉터 타입")
	private String type;

	@Column(name = "version")
	@Comment("컬렉터 버전. 가장 높은 버전만 관리하고, 이전 버전은 삭제")
	private int version;

	@Column(name = "display_name")
	@Comment("UI에 표시할 컬렉터 이름")
	private String displayName;

	@Convert(converter = ListMapToJsonStringConverter.class)
	@Column(name = "arguments", length = 1000)
	@Comment("컬렉터에 필요한 인자 정보")
	private List<Map<String, Object>> arguments;

	public static CollectorDefine create(
		String type,
		int version,
		String displayName,
		List<Map<String, Object>> arguments
	) {
		return CollectorDefine.builder()
							  .type(type)
							  .version(version)
							  .displayName(displayName)
							  .arguments(arguments)
							  .build();
	}
}
