package com.nextorm.processor.ocap.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nextorm.ocap.mail")
public class MailProperties {
		private String mailFrom;
}
