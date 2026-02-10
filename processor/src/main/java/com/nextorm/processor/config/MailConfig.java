package com.nextorm.processor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

	@Bean
	public JavaMailSender getJavaMailSender(MailProperties mailProperties) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailProperties.getHost());
		mailSender.setPort(mailProperties.getPort());
		mailSender.setUsername(mailProperties.getUsername());
		mailSender.setPassword(mailProperties.getPassword());
		mailSender.setProtocol(mailProperties.getProtocol());

		List<String> javaMailProperties = List.of("mail.smtp.auth",
			"mail.smtp.starttls.enable",
			"mail.smtp.ssl.enable",
			"mail.debug");

		Properties properties = mailSender.getJavaMailProperties();
		for (String javaMailProperty : javaMailProperties) {
			String value = mailProperties.getProperties()
										 .get(javaMailProperty);

			properties.put(javaMailProperty, value);
		}

		return mailSender;
	}
}
