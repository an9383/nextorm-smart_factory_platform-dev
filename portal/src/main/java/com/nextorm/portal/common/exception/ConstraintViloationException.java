package com.nextorm.portal.common.exception;

import com.nextorm.portal.dto.common.ConstraintViloationDto;
import lombok.Data;

import java.util.List;

@Data
public class ConstraintViloationException extends RuntimeException {
	private List<ConstraintViloationDto> data;

	public ConstraintViloationException(List<ConstraintViloationDto> data) {
		super("Constraint Violation");
		this.data = data;
	}
}