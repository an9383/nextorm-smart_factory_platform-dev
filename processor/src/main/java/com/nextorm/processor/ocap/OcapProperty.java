package com.nextorm.processor.ocap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "nextorm.ocap")
public class OcapProperty {
	/**
	 * true인 경우, 메일 발송은 하지 않고, 이력만 저장
	 */
	private boolean logOnly;
}
