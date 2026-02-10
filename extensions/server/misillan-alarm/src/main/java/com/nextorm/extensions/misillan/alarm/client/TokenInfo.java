package com.nextorm.extensions.misillan.alarm.client;

import java.time.LocalDateTime;

public record TokenInfo(String token, LocalDateTime expiredAt) {

}
