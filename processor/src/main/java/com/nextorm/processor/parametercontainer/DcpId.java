package com.nextorm.processor.parametercontainer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class DcpId {
	private Long dcpId;

	public static DcpId of(Long dcpId) {
		if (dcpId == null) {
			throw new IllegalArgumentException("dcpId는 null이면 안됩니다.");
		}
		return new DcpId(dcpId);
	}
}
