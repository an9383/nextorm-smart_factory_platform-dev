package com.nextorm.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.nextorm.portal", "com.nextorm.common.db"})
@EntityScan(basePackages = {"com.nextorm.common.db", "com.nextorm.portal.entity", "com.nextorm.common.apc.entity"})
@EnableJpaRepositories(basePackages = {"com.nextorm.common.db", "com.nextorm.portal.repository",
	"com.nextorm.common.apc.repository"})
@SpringBootApplication
public class PortalApplication {
	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}
}
