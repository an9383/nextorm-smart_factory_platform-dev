package com.nextorm.common.db.entity;

import com.nextorm.common.db.MapToJsonStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 컬렉터에서 파라미터 수집을 위해 추가로 필요한 데이터를 저장하는 엔티티
 */
@Entity
@Table(name = "parameter_extra_data")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterExtraData extends BaseEntity {
	@Column(name = "parameter_id", nullable = false, unique = true)
	private Long parameterId;

	@Column(name = "extra_data", length = 1000)
	@Convert(converter = MapToJsonStringConverter.class)
	private Map<String, Object> extraData = Map.of();

	public static ParameterExtraData create(
		Long parameterId,
		Map<String, Object> extraData
	) {
		return ParameterExtraData.builder()
								 .parameterId(parameterId)
								 .extraData(extraData)
								 .build();
	}

	public void mergeExtraData(Map<String, Object> extraData) {
		this.extraData.putAll(extraData);
	}
}
