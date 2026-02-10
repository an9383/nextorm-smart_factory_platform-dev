package com.nextorm.extensions.scheduler.task.kcs;

import java.time.LocalDateTime;

public record SfpParameterDataDto(Long parameterId, LocalDateTime traceAt, String dataType, Double dValue,
								  Integer iValue, String sValue) {
}
