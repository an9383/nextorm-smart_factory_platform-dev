package com.nextorm.apc.alertchannel;

import java.time.LocalDateTime;
import java.util.Map;

public record NotifyMessage(Long apcRequestId, String status, LocalDateTime requestTime, String sourceJsonString,
							Map<String, Object> results) {
}