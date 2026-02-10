package com.nextorm.processor.ocap.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nextorm.ocap.sms")
public class SmsProperties {
	private String accessKey;
	private String secretKey;
	private String serviceId;
	private String timeStampHeader;
	private String accessKeyHeader;
	private String signatureHeader;
	private String from;
	private String apiUrl;
	private String signatureUrl;
}
